import json
from contextlib import contextmanager

from sqlalchemy import Column, Integer, String, ForeignKey, LargeBinary
from sqlalchemy import create_engine
from sqlalchemy.dialects.mysql.types import LONGBLOB
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship

from src.storage._storage_base import StorageBase

Base = declarative_base()
CURRENT_MODEL_NAME = '20170512-110547.pb'


class Face(Base):
    __tablename__ = 'face'

    id = Column(Integer, primary_key=True)
    api_key_id = Column(ForeignKey('apikey.id'))
    face_name = Column(String(255))
    face_b_id = Column(ForeignKey('blob.id'))
    raw_b_id = Column(ForeignKey('blob.id'))

    embeddings = relationship('Embedding', uselist=True, back_populates='face')
    raw_pic = relationship('Blob', uselist=False, foreign_keys='Face.raw_b_id')
    pic = relationship('Blob', uselist=False, foreign_keys='Face.face_b_id')


class Model(Base):
    __tablename__ = 'model'

    id = Column(Integer, primary_key=True)
    api_key_id = Column(ForeignKey('apikey.id'))
    model_name = Column(String(255))
    b_id = Column(ForeignKey('blob.id'))

    blob = relationship('Blob', foreign_keys='Model.b_id')
    apikey = relationship('APIKey', foreign_keys='Model.api_key_id')


class APIKey(Base):
    __tablename__ = 'apikey'

    id = Column(Integer, primary_key=True)
    key_name = Column(String(255))


class Blob(Base):
    __tablename__ = 'blob'

    id = Column(Integer, primary_key=True)
    blob = Column(LargeBinary().with_variant(LONGBLOB, 'mysql'))


class Embedding(Base):
    __tablename__ = 'embeddings'

    id = Column(Integer, primary_key=True)
    api_key_id = Column(ForeignKey('apikey.id'))
    model_id = Column(ForeignKey('model.id'))
    blob_id = Column(ForeignKey('blob.id'))
    face_id = Column(ForeignKey('face.id'))

    face = relationship('Face', foreign_keys='Embedding.face_id')
    blob = relationship('Blob', foreign_keys='Embedding.blob_id')


class MySQLStorage(StorageBase):
    def __init__(self, db_url):
        engine = create_engine(db_url)
        Base.metadata.create_all(engine)
        self._Session = sessionmaker(bind=engine)
        self._engine = engine

    @contextmanager
    def _create_session(self):
        session = self._Session()
        yield session
        session.close()

    def get_embedding_calculator_model(self):
        with self._create_session() as session:
            model = session.query(Model).filter_by(model_name=CURRENT_MODEL_NAME).first()
            if model:
                return model.blob.blob
            else:
                raise Exception('No model available')

    def add_face(self, raw_img, face_img, embedding, face_name, api_key):
        with self._create_session() as session:
            model = session.query(Model).filter(Model.model_name == CURRENT_MODEL_NAME).first()
            api_key = session.query(APIKey).filter(APIKey.key_name == api_key).first()
            api_key_id = api_key.id
            face_blob = Blob(blob=face_img)
            raw_blob = Blob(blob=raw_img)
            face = Face(face_name=face_name, api_key_id=api_key_id)
            emb_blob = Blob(blob=json.dumps(embedding.tolist()).encode('utf-8'))
            embedding = Embedding(api_key_id=api_key_id, model_id=model.id, blob=emb_blob)
            embedding.face = face
            face.embedding = embedding
            face.pic = face_blob
            face.raw_pic = raw_blob

            session.add(face)
            session.add(embedding)
            session.commit()

    def get_classifier_training_data(self, api_key):
        with self._create_session() as session:
            model = session.query(Model).filter_by(model_name=CURRENT_MODEL_NAME).first()

            faces = session.query(Face).join(Embedding).join(APIKey) \
                .filter(Embedding.model_id == model.id) \
                .filter(APIKey.key_name == api_key) \
                .all()

            values = []
            labels = []
            face_names = {}
            curr_face_encoding = -1
            for face in faces:
                face_name = face.face_name
                if face_name not in face_names:
                    curr_face_encoding += 1
                    face_names[face_name] = curr_face_encoding
                labels.append(curr_face_encoding)
                embeddings = face.embeddings
                found = embeddings[0]
                val = json.loads(found.blob.blob.decode('utf-8'))
                values.append(val)
            return values, labels, {v: k for k, v in face_names.items()}

    def get_api_keys(self):
        with self._create_session() as session:
            return [key.key_name for key in session.query(APIKey).all()]

    def remove_face(self, api_key, face_name):
        raise NotImplementedError

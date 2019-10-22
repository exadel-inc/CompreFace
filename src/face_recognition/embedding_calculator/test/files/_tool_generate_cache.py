from src.face_recognition.embedding_calculator.test.test_calc_embedding import CACHED_MODEL_FILEPATH
from src.storage.storage_factory import get_storage

if __name__ == '__main__':
    model = get_storage().get_embedding_calculator_model()
    with CACHED_MODEL_FILEPATH.open('wb') as f:
        f.write(model)

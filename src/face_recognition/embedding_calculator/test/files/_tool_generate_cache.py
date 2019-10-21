from src.storage.storage_factory import get_storage
from src.face_recognition.embedding_calculator.test.test_calc_embedding import CACHED_MODEL_FILEPATH, \
    is_cached_model_up_to_date

if __name__ == '__main__':
    assert is_cached_model_up_to_date(), "Update CACHED_MODEL_NAME value before running this file"

    model = get_storage().get_embedding_calculator_model()
    with CACHED_MODEL_FILEPATH.open('wb') as f:
        f.write(model)

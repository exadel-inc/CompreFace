from gridfs import GridFS

from src.exceptions import NoFileFoundInDatabaseError


def save_file_to_mongo(files_fs: GridFS, filename: str, bytes_data: bytes):
    result = files_fs.find_one({"filename": filename})
    if result:
        files_fs.delete(result['_id'])

    files_fs.put(bytes_data, filename=filename)


def get_file_from_mongo(files_fs: GridFS, filename: str):
    result = files_fs.find_one({"filename": filename})
    if result is None:
        raise NoFileFoundInDatabaseError(f'File with filename {filename} is not found in the database')
    return result.read()

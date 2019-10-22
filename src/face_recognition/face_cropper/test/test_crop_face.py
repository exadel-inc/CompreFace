from mock import Mock

from src.face_recognition.face_cropper.cropper import crop_face


def test__when_called_with__then_calls_crop_faces_and_takes_the_first_element(mocker):
    img, face1 = object(), object()
    crop_faces_mock: Mock = mocker.patch('src.face_recognition.face_cropper.cropper.crop_faces', return_value=[face1])

    cropped_face = crop_face(img)

    crop_faces_mock.assert_called_once_with(img, face_lim=1)
    assert cropped_face == face1

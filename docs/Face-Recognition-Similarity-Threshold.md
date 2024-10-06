# Face Recognition Similarity Threshold

The result of CompreFace face recognition and face verification services
is a similarity between faces. Even if you upload the faces of two
different people, you still receive the result, but the similarity is
low. Therefore, the user must determine for himself whether this is the
same person or not using similarity. The level of similarity the user
accepts, as big enough, we call similarity threshold.

## How to choose the face similarity threshold

No Face Recognition Service has 100% accuracy, so there always appear
errors in recognition. If a user chooses too low a threshold, then some
unknown faces are recognized as known. If a user chooses too high a
threshold, then some known faces are recognized as unknown. CompreFace
calculates similarity so that most correct guesses have a threshold of
more than 0.5, and the most incorrect guesses have a threshold of less
than 0.5. Still, we recommend for high-security systems set the
threshold more than 0.5. This is the distribution of similarities for a
custom dataset of 50,000 faces for the FaceNet model (blue - is
incorrect guesses, red is correct):

<img src="https://user-images.githubusercontent.com/3736126/111870491-bb422380-898d-11eb-901d-0fad65eee69c.png" alt="distribution of similarities" width=800px style="padding: 10px;">

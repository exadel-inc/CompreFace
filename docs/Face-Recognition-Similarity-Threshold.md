# Face Recognition Similarity Threshold

The result of CompreFace face recognition and face verification services is a similarity between faces. Even if you upload faces of two different people, you will still receive the result, but the similarity will be low. The user must determine for himself whether this is the same person or not using similarity.
The level of similarity the user accepts, as big enough, we call similarity threshold.

## How to choose the face similarity threshold

No Face Recognition Service has 100% accuracy, so there always will be errors in recognition.
If a user chooses too low threshold, then some unknown faces will be recognized as known.
If a user chooses too high threshold, then some known faces will be recognized as unknown.
CompreFace calculates similarity in a way, so most correct guesses will be with a threshold of more than 0.5, and the most incorrect guesses will be with a threshold of less than 0.5. Still, we recommend for high-security systems set the threshold more than 0.5.
This is the distribution of similarities for a custom dataset of 50,000 faces for FaceNet model (blue - is incorrect guesses, red is correct):
<img src="https://user-images.githubusercontent.com/3736126/111870491-bb422380-898d-11eb-901d-0fad65eee69c.png" alt="distribution of similarities" width=800px style="padding: 10px;">
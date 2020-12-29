/* eslint-disable @typescript-eslint/naming-convention */
export interface RequestResult {
  box: {
    probability: {
      x_max: number;
      x_min: number;
      y_max: number;
      y_min: number;
    },
    faces: [
      {
        face_name: string;
        similarity: number;
      }
    ]
  }
}

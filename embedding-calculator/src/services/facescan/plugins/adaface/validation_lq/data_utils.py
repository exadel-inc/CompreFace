import cv2
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
from PIL import Image

class ListDatasetWithIndex(Dataset):
    def __init__(self, img_list, image_is_saved_with_swapped_B_and_R=False):
        super(ListDatasetWithIndex, self).__init__()

        # image_is_saved_with_swapped_B_and_R: correctly saved image should have this set to False
        # face_emore/img has images saved with B and G (of RGB) swapped. 
        # Since training data loader uses PIL (results in RGB) to read image 
        # and validation data loader uses cv2 (results in BGR) to read image, this swap was okay.
        # But if you want to evaluate on the training data such as face_emore/img (B and G swapped), 
        # then you should set image_is_saved_with_swapped_B_and_R=True

        self.img_list = img_list
        self.transform = transforms.Compose([
                    transforms.ToTensor(),
                    transforms.Normalize([0.5, 0.5, 0.5], [0.5, 0.5, 0.5])
                ])
        self.image_is_saved_with_swapped_B_and_R = image_is_saved_with_swapped_B_and_R

    def __len__(self):
        return len(self.img_list)

    def __getitem__(self, idx):

        if self.image_is_saved_with_swapped_B_and_R:
            with open(self.img_list[idx], 'rb') as f:
                img = Image.open(f)
                img = img.convert('RGB')
            img = self.transform(img)

        else:
            # ArcFace Pytorch
            img = cv2.imread(self.img_list[idx])
            # img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            img = img[:,:,:3]
            
            img = Image.fromarray(img)
            # img = np.moveaxis(img, -1, 0)
            img = self.transform(img)
        return img, idx


class ListDataset(Dataset):
    def __init__(self, img_list, image_is_saved_with_swapped_B_and_R=False):
        super(ListDataset, self).__init__()

        # image_is_saved_with_swapped_B_and_R: correctly saved image should have this set to False
        # face_emore/img has images saved with B and G (of RGB) swapped.
        # Since training data loader uses PIL (results in RGB) to read image
        # and validation data loader uses cv2 (results in BGR) to read image, this swap was okay.
        # But if you want to evaluate on the training data such as face_emore/img (B and G swapped),
        # then you should set image_is_saved_with_swapped_B_and_R=True

        self.img_list = img_list
        self.transform = transforms.Compose(
            [transforms.ToTensor(), transforms.Normalize([0.5, 0.5, 0.5], [0.5, 0.5, 0.5])])

        self.image_is_saved_with_swapped_B_and_R = image_is_saved_with_swapped_B_and_R


    def __len__(self):
        return len(self.img_list)

    def __getitem__(self, idx):
        image_path = self.img_list[idx]
        img = cv2.imread(image_path)
        img = img[:, :, :3]

        if self.image_is_saved_with_swapped_B_and_R:
            print('check if it really should be on')
            img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

        img = Image.fromarray(img)
        img = self.transform(img)
        return img, idx


def prepare_imagelist_dataloader(img_list, batch_size, num_workers=0, image_is_saved_with_swapped_B_and_R=False):
    # image_is_saved_with_swapped_B_and_R: correctly saved image should have this set to False
    # face_emore/img has images saved with B and G (of RGB) swapped. 
    # Since training data loader uses PIL (results in RGB) to read image 
    # and validation data loader uses cv2 (results in BGR) to read image, this swap was okay.
    # But if you want to evaluate on the training data such as face_emore/img (B and G swapped), 
    # then you should set image_is_saved_with_swapped_B_and_R=True

    image_dataset = ListDatasetWithIndex(img_list, image_is_saved_with_swapped_B_and_R)
    dataloader = DataLoader(image_dataset, batch_size=batch_size, shuffle=False, drop_last=False, num_workers=num_workers)
    return dataloader


def prepare_dataloader(img_list, batch_size, num_workers=0, image_is_saved_with_swapped_B_and_R=False):
    # image_is_saved_with_swapped_B_and_R: correctly saved image should have this set to False
    # face_emore/img has images saved with B and G (of RGB) swapped.
    # Since training data loader uses PIL (results in RGB) to read image
    # and validation data loader uses cv2 (results in BGR) to read image, this swap was okay.
    # But if you want to evaluate on the training data such as face_emore/img (B and G swapped),
    # then you should set image_is_saved_with_swapped_B_and_R=True

    image_dataset = ListDataset(img_list, image_is_saved_with_swapped_B_and_R=image_is_saved_with_swapped_B_and_R)
    dataloader = DataLoader(image_dataset,
                            batch_size=batch_size,
                            shuffle=False,
                            drop_last=False,
                            num_workers=num_workers)
    return dataloader
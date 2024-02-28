
python main.py \
    --data_root /mnt/home/kimminc2/data \
    --train_data_path faces_emore \
    --val_data_path faces_emore \
    --prefix ir101_ms1mv2_adaface \
    --use_wandb \
    --use_mxrecord \
    --gpus 2 \
    --use_16bit \
    --arch ir_101 \
    --batch_size 512 \
    --num_workers 16 \
    --epochs 26 \
    --lr_milestones 12,20,24 \
    --lr 0.1 \
    --head adaface \
    --m 0.4 \
    --h 0.333 \
    --low_res_augmentation_prob 0.2 \
    --crop_augmentation_prob 0.2 \
    --photometric_augmentation_prob 0.2


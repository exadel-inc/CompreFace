DATAROOT=/data/data/faces

python main.py \
    --data_root ${DATAROOT} \
    --train_data_path faces_emore/imgs \
    --val_data_path faces_emore \
    --prefix adaface_ir50_ms1mv2 \
    --gpus 1 \
    --use_16bit \
    --start_from_model_statedict ./pretrained/adaface_ir50_ms1mv2.ckpt \
    --arch ir_50 \
    --evaluate

python main.py \
    --data_root ${DATAROOT} \
    --train_data_path faces_emore/imgs \
    --val_data_path faces_emore \
    --prefix adaface_ir101_ms1mv2 \
    --gpus 1 \
    --use_16bit \
    --start_from_model_statedict ./pretrained/adaface_ir101_ms1mv2.ckpt \
    --arch ir_101 \
    --evaluate

python main.py \
    --data_root ${DATAROOT} \
    --train_data_path faces_emore/imgs \
    --val_data_path faces_emore \
    --prefix adaface_ir101_ms1mv3 \
    --gpus 1 \
    --use_16bit \
    --start_from_model_statedict ./pretrained/adaface_ir101_ms1mv3.ckpt \
    --arch ir_101 \
    --evaluate

python main.py \
    --data_root ${DATAROOT} \
    --train_data_path faces_emore/imgs \
    --val_data_path faces_emore \
    --prefix adaface_ir101_webface4m \
    --gpus 1 \
    --use_16bit \
    --start_from_model_statedict ./pretrained/adaface_ir101_webface4m.ckpt \
    --arch ir_101 \
    --evaluate

python main.py \
    --data_root ${DATAROOT} \
    --train_data_path faces_emore/imgs \
    --val_data_path faces_emore \
    --prefix adaface_ir101_webface12m \
    --gpus 1 \
    --use_16bit \
    --start_from_model_statedict ./pretrained/adaface_ir101_webface12m.ckpt \
    --arch ir_101 \
    --evaluate
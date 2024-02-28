import torch
import torch.nn.parallel
import torch.utils.data
import torch.utils.data.distributed
import pytorch_lightning as pl
from pytorch_lightning.callbacks import ModelCheckpoint
from pytorch_lightning.loggers import CSVLogger, WandbLogger
from pytorch_lightning import seed_everything
import config
import os
from utils import dotdict
import train_val
import data
import inspect

def main(args):

    hparams = dotdict(vars(args))

    trainer_mod = train_val.Trainer(**hparams)
    data_mod = data.DataModule(**hparams)

    if hparams.seed is not None:
        seed_everything(hparams.seed)

    # create model checkpoint callback
    monitor = 'val_acc'
    mode = 'max'
    save_top_k = hparams.epochs+1 if hparams.save_all_models else 1
    checkpoint_callback = ModelCheckpoint(dirpath=hparams.output_dir, save_last=True,
                                          save_top_k=save_top_k, monitor=monitor, mode=mode)

    # create logger
    csv_logger = CSVLogger(save_dir=hparams.output_dir, name='result')
    my_loggers = [csv_logger]
    if args.use_wandb:
        wandb_logger = WandbLogger(save_dir=hparams.output_dir,
                                   name=os.path.basename(args.output_dir), project='adaface_face_recognition')
        my_loggers.append(wandb_logger)

    resume_from_checkpoint = hparams.resume_from_checkpoint if hparams.resume_from_checkpoint else None

    params = inspect.signature(pl.Trainer).parameters.values()
    if 'strategy' in [param.name for param in params]:
        # recent pytorch lightning
        trainer = pl.Trainer(resume_from_checkpoint=resume_from_checkpoint,
                             default_root_dir=hparams.output_dir,
                             logger=my_loggers,
                             gpus=hparams.gpus,
                             max_epochs=hparams.epochs,
                             accelerator='cpu' if hparams.gpus == 0 else 'gpu',
                             strategy=hparams.distributed_backend,
                             precision=16 if hparams.use_16bit else 32,
                             fast_dev_run=hparams.fast_dev_run,
                             callbacks=[checkpoint_callback],
                             num_sanity_val_steps=16 if hparams.batch_size > 63 else 100,
                             val_check_interval=1.0 if hparams.epochs > 4 else 0.1,
                             accumulate_grad_batches=hparams.accumulate_grad_batches,
                             limit_train_batches=50 if hparams.test_run else 1.0
                             )
    else:
        # pytorch lightning before 1.4.4
        trainer = pl.Trainer(resume_from_checkpoint=resume_from_checkpoint,
                             default_root_dir=hparams.output_dir,
                             logger=my_loggers,
                             gpus=hparams.gpus,
                             max_epochs=hparams.epochs,
                             accelerator=hparams.distributed_backend,
                             precision=16 if hparams.use_16bit else 32,
                             fast_dev_run=hparams.fast_dev_run,
                             callbacks=[checkpoint_callback],
                             num_sanity_val_steps=16 if hparams.batch_size > 63 else 100,
                             val_check_interval=1.0 if hparams.epochs > 4 else 0.1,
                             accumulate_grad_batches=hparams.accumulate_grad_batches,
                             limit_train_batches=50 if hparams.test_run else 1.0
                             )

    if not hparams.evaluate:
        # train / val
        print('start training')
        trainer.fit(trainer_mod, data_mod)
        print('start evaluating')
        print('evaluating from ', checkpoint_callback.best_model_path)
        trainer.test(ckpt_path='best', datamodule=data_mod)
    else:
        # eval only
        print('start evaluating')
        trainer.test(trainer_mod, datamodule=data_mod)


if __name__ == '__main__':

    args = config.get_args()

    if args.distributed_backend == 'ddp' and args.gpus > 0:
        # When using a single GPU per process and per
        # DistributedDataParallel, we need to divide the batch size
        # ourselves based on the total number of GPUs we have
        torch.set_num_threads(1)
        args.total_batch_size = args.batch_size
        args.batch_size = int(args.batch_size / max(1, args.gpus))
        args.num_workers = min(args.num_workers, 16)

    if args.resume_from_checkpoint:
        assert args.resume_from_checkpoint.endswith('.ckpt')
        args.output_dir = os.path.dirname(args.resume_from_checkpoint)
        print('resume from {}'.format(args.output_dir))

    main(args)

# Face data migration

## When do you need to migrate data

When you upload a new known image to the Face Collection, CompreFace
uses a neural network model to calculate an embedding (also known as
face features), which is an array of 512 or 128 numbers. Then CompreFace
saves it to the database to use in the future comparison - when you
upload a face to recognize, CompreFace again calculates an embedding and
compares it to saved embeddings.

The important thing here is that every neural network model calculates
different embeddings. Therefore, it means that it makes sense to compare
embeddings calculated by the same neural network model.

CompreFace doesn't change the neural network model during its work, so
you usually don't need to migrate your face data. If you want to try
[custom build](Custom-builds.md), be very careful - look at the table
[here](../custom-builds/README.md), column `Face recognition model` - if
the model changed, you need to run a migration.

## Limitations

If you run CompreFace in the ["not saving images to database"
mode](Configuration.md)(`save_images_to_db=false`), you won't be
able to migrate data as the original images are required for migration.

The only solution here is to delete all images from Face Collection and
upload them again.

## How to perform a migration

Current migration was written for internal usage and wasn't tested
enough, so please do a backup copy of the database and perform migration
at your own risk.

REST request to start migration:

    curl -i -X POST \
    'http://localhost:8000/api/v1/migrate'

This rest endpoint is asynchronous; it starts the migration and returns
a response immediately. Please look at logs for "Migration successfully
finished" text to understand if the migration is successful.

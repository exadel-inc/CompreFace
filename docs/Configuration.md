# Configuration

In the [release](https://github.com/exadel-inc/CompreFace/releases)
archive and all custom builds, there is a `.env` file with configuration
options for CompreFace. For production systems, we recommend looking
through them and set up CompreFace accordingly

-   `registry` - this is the docker hub registry. For release and
    pre-build images, it should be set to `exadel/` value
-   `postgres_password` - password for Postgres database. It should be
    changed for production systems from the default value.
-   `postgres_domain` - the domain where Postgres database is run
-   `postgres_port` - Postgres database port
-   `enable_email_server` - if true, it enables email verification for
    users. You should set email_host, email_username, and email_password
    variables for the correct work.
-   `email_host` - a host of the email provider. It should be set if the
    `enable_email_server` variable is true
-   `email_username` - a username of email provider for authentication.
    It should be set if the `enable_email_server` variable is true
-   `email_password` - a password of the email provider for
    authentication. It should be set if the `enable_email_server`
    variable is true
-   `email_from` - this value reads users in the `from` fields when
    they receive emails from CompreFace. Corresponds to `From` field in
    rfc2822. Optional, if not set, then `email_username` is used instead
-   `save_images_to_db` - should the CompreFace save photos to the
    database. Be careful, [migrations](Face-data-migration.md) could be
    run only if this value is `true`. Doesn't work in 0.6.0 and 0.6.1 version, please use 0.5.1 version or >0.6.1 version instead
-   `compreface_api_java_options` - java options of compreface-api
    container
-   `compreface_admin_java_options` - java options of compreface-admin
    container
-   `ADMIN_VERSION` - docker image tag of the compreface-admin container
-   `API_VERSION` - docker image tag of the compreface-api container
-   `FE_VERSION` - docker image tag of the compreface-fe container
-   `CORE_VERSION` - docker image tag of the compreface-core container

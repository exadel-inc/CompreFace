import subprocess
import sys

from src.services.facescan.plugins import helpers


def install_requirements(requirements: set):
    print(f'Install dependencies: {requirements}')
    cmd = f"{sys.executable} -m pip install --no-cache-dir {' '.join(requirements)}"
    try:
        subprocess.run(cmd.split(), check=True)
    except subprocess.CalledProcessError:
        exit(1)


if __name__ == '__main__':
    plugins = helpers.get_face_plugins()
    plugins.append(helpers.get_detector())

    dependencies = set()
    for plugin in plugins:
        dependencies |= set(plugin.dependencies)
    install_requirements(dependencies)

    for plugin in plugins:
        if plugin.ml_model:
            print(f'Checking models for {plugin}...')
            plugin.ml_model.download_if_not_exists()

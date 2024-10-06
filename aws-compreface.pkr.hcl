packer {
  required_plugins {
    amazon = {
      version = ">= 0.0.1"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "compreface" {
  ami_name      = "compreface_image_1.1.0"
  instance_type = "t2.medium"
  region        = "us-east-1"
  source_ami_filter {
    filters = {
      name                = "amzn2-ami-kernel-5.10-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["137112412989"]
  }
  ssh_username = "ec2-user"
}

build {
  name    = "packer"
  sources = [
    "source.amazon-ebs.compreface"
  ]
  provisioner "shell" {
    inline = [
      "sudo amazon-linux-extras install docker",
      "sudo service docker start",
      "sudo usermod -a -G docker ec2-user",
      "sudo chkconfig docker on",
      "sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose",
      "sudo chmod +x /usr/local/bin/docker-compose",
      "docker-compose version",
      "wget -q -O tmp.zip 'https://github.com/exadel-inc/CompreFace/releases/download/v1.1.0/CompreFace_1.1.0.zip' && unzip tmp.zip && rm tmp.zip && rm /home/ec2-user/.ssh/authorized_keys && sudo rm /root/.ssh/authorized_keys",
      "sudo chmod 666 /var/run/docker.sock",
      "docker-compose up -d"
    ]
  }
}

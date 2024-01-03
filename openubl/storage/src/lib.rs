use anyhow::anyhow;
use std::fs::rename;
use std::path::Path;
use std::str::FromStr;

use minio::s3::args::UploadObjectArgs;
use minio::s3::client::Client;
use minio::s3::creds::StaticProvider;
use minio::s3::http::BaseUrl;
use uuid::Uuid;

use crate::config::Storage;

pub mod config;

pub enum StorageSystem {
    FileSystem(String),
    Minio(String, Client),
}

#[derive(Debug, thiserror::Error)]
pub enum StorageSystemErr {
    #[error(transparent)]
    Filesystem(std::io::Error),
    #[error(transparent)]
    Minio(minio::s3::error::Error),
}

impl From<std::io::Error> for StorageSystemErr {
    fn from(e: std::io::Error) -> Self {
        Self::Filesystem(e)
    }
}

impl From<minio::s3::error::Error> for StorageSystemErr {
    fn from(e: minio::s3::error::Error) -> Self {
        Self::Minio(e)
    }
}

impl StorageSystem {
    pub fn new(config: &Storage) -> anyhow::Result<Self> {
        match config.storage_type.as_str() {
            "filesystem" => Ok(Self::FileSystem(config.filesystem.dir.clone())),
            "minio" => {
                let static_provider =
                    StaticProvider::new(&config.minio.access_key, &config.minio.secret_key, None);
                let client = Client::new(
                    BaseUrl::from_str(&config.minio.host)?,
                    Some(Box::new(static_provider)),
                    None,
                    None,
                )?;
                Ok(Self::Minio(config.minio.bucket.clone(), client))
            }
            _ => Err(anyhow!("Not supported storage type")),
        }
    }

    pub async fn upload(&self, filename: &str) -> Result<String, StorageSystemErr> {
        let object_id = Uuid::new_v4().to_string();
        match self {
            StorageSystem::FileSystem(workspace) => {
                let new_path = Path::new(workspace).join(&object_id);
                rename(filename, new_path)?;
                Ok(object_id.clone())
            }
            StorageSystem::Minio(bucket, client) => {
                let object = &UploadObjectArgs::new(bucket, &object_id, filename)?;
                let response = client.upload_object(object).await?;
                Ok(response.object_name)
            }
        }
    }
}

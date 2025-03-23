import axios from "axios";

const S3_API_BASE = "/api/s3";

export interface S3Object {
  name: string;
  size: number;
  lastModified: string;
  etag: string;
}

export interface S3Bucket {
  name: string;
  creationDate: string;
}

export const s3Service = {
  // List buckets
  listBuckets: async (): Promise<S3Bucket[]> => {
    const response = await axios.get(`${S3_API_BASE}/api/v1/buckets`);
    return response.data.buckets;
  },

  // List files in a bucket
  listFiles: async (bucket: string, prefix = ""): Promise<S3Object[]> => {
    const response = await axios.get(
      `${S3_API_BASE}/api/v1/buckets/${bucket}/objects`,
      {
        params: { prefix },
      }
    );
    return response.data.objects;
  },

  // Get file details
  getFileDetails: async (bucket: string, path: string): Promise<S3Object> => {
    const response = await axios.get(
      `${S3_API_BASE}/api/v1/buckets/${bucket}/objects/download`,
      {
        params: {
          prefix: path,
          preview: true,
        },
      }
    );
    return response.data;
  },

  // Upload a file
  uploadFile: async (
    bucket: string,
    file: File,
    path: string
  ): Promise<void> => {
    const formData = new FormData();
    formData.append("file", file);

    await axios.put(`${S3_API_BASE}/${bucket}/${path}`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  // Download a file
  downloadFile: async (bucket: string, path: string): Promise<Blob> => {
    const response = await axios.get(`${S3_API_BASE}/${bucket}/${path}`, {
      responseType: "blob",
    });
    return response.data;
  },

  // Delete a file
  deleteFile: async (bucket: string, path: string): Promise<void> => {
    await axios.delete(`${S3_API_BASE}/${bucket}/${path}`);
  },
};

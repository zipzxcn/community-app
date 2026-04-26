export interface UploadTokenPayload {
  bizType: string
  fileName: string
  contentType: string
  size: number
}

export interface UploadToken {
  provider: string
  uploadUrl: string
  bucketName: string
  objectKey: string
  headers: Record<string, string>
}

export interface CompleteUploadPayload {
  bizType: string
  bizId?: number
  bucketName: string
  objectKey: string
  accessUrl: string
  originalName: string
  mimeType: string
  sizeBytes: number
}

export interface FileObject {
  id: number
  accessUrl: string
  originalName: string
  mimeType: string
  sizeBytes: number
  bizType: string
  bizId?: number
}

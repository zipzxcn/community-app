import axios from 'axios'
import client from '@/api/client'
import type { CompleteUploadPayload, FileObject, UploadToken, UploadTokenPayload } from '@/types/file'

export function createUploadToken(payload: UploadTokenPayload) {
  return client.post<never, UploadToken>('/files/upload-token', payload)
}

export function completeUpload(payload: CompleteUploadPayload) {
  return client.post<never, FileObject>('/files/complete', payload)
}

export async function uploadImageFile(file: File, bizType: string) {
  const token = await createUploadToken({
    bizType,
    fileName: file.name,
    contentType: file.type || 'application/octet-stream',
    size: file.size,
  })
  await axios.put(token.uploadUrl, file, {
    headers: token.headers,
  })
  const accessUrl = token.uploadUrl.split('?')[0]
  return completeUpload({
    bizType,
    bucketName: token.bucketName,
    objectKey: token.objectKey,
    accessUrl,
    originalName: file.name,
    mimeType: file.type || 'application/octet-stream',
    sizeBytes: file.size,
  })
}

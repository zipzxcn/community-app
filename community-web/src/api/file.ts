/**
 * 文件接口封装：前端先申请上传凭证，再直传对象存储，最后回调后端登记元数据。
 */
import axios from 'axios'
import client from '@/api/client'
import type { CompleteUploadPayload, FileObject, UploadToken, UploadTokenPayload } from '@/types/file'

/**
 * createUploadToken：按函数名对应后端接口完成请求封装。
 */
export function createUploadToken(payload: UploadTokenPayload) {
  return client.post<never, UploadToken>('/files/upload-token', payload)
}

/**
 * completeUpload：按函数名对应后端接口完成请求封装。
 */
export function completeUpload(payload: CompleteUploadPayload) {
  return client.post<never, FileObject>('/files/complete', payload)
}

/**
 * deleteFile：按函数名对应后端接口完成请求封装。
 */
export function deleteFile(fileId: number) {
  return client.delete<never, void>(`/files/${fileId}`)
}

/**
 * uploadImageFile：按函数名对应后端接口完成请求封装。
 */
export async function uploadImageFile(file: File, bizType: string) {
  const token = await createUploadToken({
    bizType,
    fileName: file.name,
    contentType: file.type || 'application/octet-stream',
    size: file.size,
  })
  // 直传文件到对象存储
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

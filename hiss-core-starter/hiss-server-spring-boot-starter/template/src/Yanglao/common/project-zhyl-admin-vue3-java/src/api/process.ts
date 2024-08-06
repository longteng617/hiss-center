import { request } from '@/utils/request'
import type { ListResult } from '@/api/model/synergyModel'

// 获取流程分类的列表
export function getProcessCategoryList(params) {
  return request.post<ListResult>({
    url: '/process/list/category',
    params
  })
}

// 获取流程分类下的流程列表
export function getCategoryProcessList(id) {
  return request.post<ListResult>({
    url: `/process/list/category/process/${id}`
  })
}

// 获取个人申请的流程
export function getUserApplayProcessList(params) {
  return request.post<ListResult>({
    url: `/process/applay/list`,
    params
  })
}

// 删除个人申请的流程
export function delUserApplayProcess(id) {
  return request.post<ListResult>({
    url: `/process/applay/delete/${id}`
  })
}

// 获取个人办理的流程
export function getUserHandleProcessList(params) {
  return request.post<ListResult>({
    url: `/process/handle/list`,
    params
  })
}

// 获取个人申请的流程
export function startBisProcess(id) {
  return request.post<ListResult>({
    url: `/process/start/${id}`
  })
}

import request from '@/utils/request'

// 获取流程分类的列表
export function getProcessCategoryList(params) {
  return request({
    url: '/process/list/category',
    method: 'post',
    data: params
  })
}

// 获取表单分类的列表
export function getFormCategoryList(params) {
  return request({
    url: '/process/form/category',
    method: 'post',
    data: params
  })
}

// 获取流程分类下的流程列表
export function getCategoryProcessList(id) {
  return request({
    url: `/process/list/category/process/${id}`,
    method: 'post'
  })
}

// 流程列表
export function getProcessBisList(params) {
  return request({
    url: `/process/list/bis`,
    method: 'post',
    data: params
  })
}

// 流程列表
export function getProcessDevList(params) {
  return request({
    url: `/process/list/dev`,
    method: 'post',
    data: params
  })
}

// 删除流程模型
export function delProcessModel(id) {
  return request({
    url: `/process/model/delete/${id}`,
    method: 'post'
  })
}

// 获取流程实例
export function getProcessInstanceList(params) {
  return request({
    url: `/process/list/instance`,
    method: 'post',
    data: params
  })
}

// 获取表单列表
export function getFormList(params) {
  return request({
    url: `/process/form/list`,
    method: 'post',
    data: params
  })
}


// 获取个人申请的流程
export function getUserApplayProcessList(params) {
  return request({
    url: `/process/applay/list`,
    method: 'post',
    data: params
  })
}

// 删除表单实例
export function delFormModel(id) {
  return request({
    url: `/process/form/delete/${id}`,
    method: 'post'
  })
}

// 删除流程实例
export function delProcessInstance(id) {
  return request({
    url: `/process/instance/delete/${id}`,
    method: 'post'
  })
}


// 删除个人申请的流程
export function delUserApplayProcess(id) {
  return request({
    url: `/process/applay/delete/${id}`,
    method: 'post'
  })
}

// 获取个人办理的流程
export function getUserHandleProcessList(params) {
  return request({
    url: `/process/handle/list`,
    method: 'post',
    data: params
  })
}

// 获取个人申请的流程
export function startBisProcess(id) {
  return request({
    url: `/process/start/${id}`,
    method: 'post'
  })
}

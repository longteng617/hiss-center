export const HISS_BASE_URL = 'http://#hiss{host}:#hiss{httpPort}/'

export const PROCESS_APPLAY_STATUS = {
  PREPARE: {
    label: '待发起'
  },
  ACTIVE: {
    label: '办理中'
  },
  COMPLETE: {
    label: '已完成'
  },
  CANCEL: {
    label: '已取消'
  }
}

export function getMessageAuth(){
  let item = localStorage.getItem('hiss_user');
  if(item){
    item = JSON.parse(item)
  }else{
    item = {}
  }
  const messageAuth = { currentUser:{} }
  if(item['appId']){ // 记录当前的应用ID
    messageAuth['tenant'] = item['appId']
  }
  if(item['userId']){ // 记录当前的用户ID
    messageAuth.currentUser['userId'] = item['userId']
  }
  if(item['userName']){ // 记录当前的用户名称
    messageAuth.currentUser['userName'] = item['userName']
  }
  return messageAuth
}

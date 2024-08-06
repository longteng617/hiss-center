// 获取常用时间
import dayjs from 'dayjs'
export const COLUMNS = [
  {
    title: '序号',
    align: 'left',
    width: 70,
    minWidth: 70,
    colKey: 'rowIndex'
  },
  {
    title: '流程名',
    width: 300,
    minWidth: 300,
    colKey: 'name'
  },
  {
    title: '业务号',
    minWidth: 180,
    colKey: 'businessKey'
  },
  {
    title: '耗时',
    minWidth: 180,
    colKey: 'duration'
  },
  {
    title: '申请时间',
    minWidth: 120,
    colKey: 'startTime',
    cell: (h, { row }) => {
      return h(
        'span',
        {},
        row.startTime
          ? dayjs(Number(row.startTime)).format('YYYY-MM-DD HH:mm')
          : ''
      )
    }
  },
  {
    title: '完成时间',
    minWidth: 120,
    colKey: 'endTime',
    cell: (h, { row }) => {
      return h(
        'span',
        {},
        row.endTime
          ? dayjs(Number(row.endTime)).format('YYYY-MM-DD HH:mm')
          : ''
      )
    }
  },
  {
    title: '流程状态',
    colKey: 'status',
    width: 120,
    minWidth: '120px',
    cell: (h, { row }) => {
      const statusList = {
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
      return h(
        'span',
        {
          class: `status-dot status-dot-${row.status}`
        },
        statusList[row.status].label
      )
    }
  },
  {
    align: 'left',
    fixed: 'right',
    width: 110,
    colKey: 'op',
    title: '操作'
  }
]

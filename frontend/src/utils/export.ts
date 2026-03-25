import { saveAs } from 'file-saver'

/**
 * 导出纯文本为 .txt 文件
 */
export function exportTxt(content: string, filename = 'document') {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  saveAs(blob, `${filename}.txt`)
}

/**
 * 导出表格数据为 CSV
 */
export function exportCsv(columns: string[], rows: Record<string, unknown>[], filename = 'data') {
  const header = columns.join(',')
  const body = rows
    .map(row => columns.map(col => {
      const val = String(row[col] ?? '')
      return val.includes(',') ? `"${val}"` : val
    }).join(','))
    .join('\n')
  const bom = '\uFEFF' // UTF-8 BOM，解决Excel乱码
  const blob = new Blob([bom + header + '\n' + body], { type: 'text/csv;charset=utf-8' })
  saveAs(blob, `${filename}.csv`)
}

/**
 * 导出 HTML 字符串为 Word (.doc)
 */
export function exportWord(htmlContent: string, filename = 'document') {
  const header = `<html xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:w="urn:schemas-microsoft-com:office:word"
    xmlns="http://www.w3.org/TR/REC-html40">
    <head><meta charset="utf-8"><title>${filename}</title></head><body>`
  const footer = '</body></html>'
  const blob = new Blob([header + htmlContent + footer], {
    type: 'application/msword',
  })
  saveAs(blob, `${filename}.doc`)
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

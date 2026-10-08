import * as XLSX from 'xlsx';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';

export interface ObservationExportRow {
  id: number;
  createdAt: string;
  author: string;
  greenhouse: string;
  zone: string;
  details: string;
}

export function downloadObservationsXlsx(reports: ObservationExportRow[]): void {
  const rows = reports.map((r) => ({
    Дата: new Date(r.createdAt).toLocaleString('ru-RU'),
    Автор: r.author,
    Теплица: r.greenhouse,
    Секция: r.zone,
    Наблюдение: r.details
  }));
  const ws = XLSX.utils.json_to_sheet(rows);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Наблюдения');
  const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
  XLSX.writeFile(wb, `zhurnal-nablyudeniy-${stamp}.xlsx`);
}

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export async function downloadObservationsPdf(reports: ObservationExportRow[]): Promise<void> {
  const wrap = document.createElement('div');
  wrap.style.cssText =
    'position:fixed;left:-12000px;top:0;width:720px;padding:20px 24px 28px;background:#fff;color:#1a1a1a;' +
    'font:14px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif;box-sizing:border-box;';

  let inner = '<h1 style="margin:0 0 18px;font-size:20px;color:#0c4a2a;font-weight:700">Журнал наблюдений</h1>';
  for (const r of reports) {
    inner += `<section style="margin-bottom:18px;border:1px solid #d4e5dc;border-radius:10px;padding:12px 14px;background:#fafdfb">`;
    inner += `<div style="font-weight:700;margin-bottom:6px">${escapeHtml(r.greenhouse)} / ${escapeHtml(r.zone)}</div>`;
    inner += `<div style="font-size:12px;color:#5a6570;margin-bottom:8px">${escapeHtml(r.author)} — ${escapeHtml(
      new Date(r.createdAt).toLocaleString('ru-RU')
    )}</div>`;
    inner += `<div style="white-space:pre-wrap;word-break:break-word">${escapeHtml(r.details || '—')}</div>`;
    inner += `</section>`;
  }
  wrap.innerHTML = inner;
  document.body.appendChild(wrap);

  try {
    const canvas = await html2canvas(wrap, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    });
    const imgData = canvas.toDataURL('image/png', 1.0);
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const pageW = doc.internal.pageSize.getWidth();
    const pageH = doc.internal.pageSize.getHeight();
    const imgW = pageW;
    const imgH = (canvas.height * imgW) / canvas.width;

    let heightLeft = imgH;
    let position = 0;
    doc.addImage(imgData, 'PNG', 0, position, imgW, imgH);
    heightLeft -= pageH;

    while (heightLeft > 0) {
      position = heightLeft - imgH;
      doc.addPage();
      doc.addImage(imgData, 'PNG', 0, position, imgW, imgH);
      heightLeft -= pageH;
    }

    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
    doc.save(`zhurnal-nablyudeniy-${stamp}.pdf`);
  } finally {
    wrap.remove();
  }
}

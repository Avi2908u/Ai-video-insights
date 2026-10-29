(() => {
  const API_BASE = (window.VIDEO_INSIGHTS_API_URL || localStorage.getItem('avi-api-base') || 'http://localhost:8080').replace(/\/$/, '');
  const STORAGE_KEY = 'ai-video-insights-library-v1';
  const content = document.querySelector('#page-content');
  const pageTitle = document.querySelector('#page-title');
  const saved = readLibrary();
  let currentPage = 'overview';
  let activeResult = null;
  let resultTab = 'summary';
  let toastTimer;

  function readLibrary() { try { const data = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]'); return Array.isArray(data) ? data : []; } catch { return []; } }
  function persist() { localStorage.setItem(STORAGE_KEY, JSON.stringify(saved)); }
  function esc(value = '') { return String(value).replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char])); }
  function videoId(url) { try { const parsed = new URL(url); if (parsed.hostname.includes('youtu.be')) return parsed.pathname.slice(1).split('/')[0]; if (parsed.searchParams.get('v')) return parsed.searchParams.get('v'); const parts = parsed.pathname.split('/').filter(Boolean); const marker = parts.findIndex(part => ['embed','shorts','live'].includes(part)); return marker >= 0 ? parts[marker + 1] : ''; } catch { return ''; } }
  function validYoutube(url) { const id = videoId(url); return Boolean(id && /^[\w-]{6,}$/.test(id) && /(^|\.)youtube\.com$|(^|\.)youtu\.be$/i.test(new URL(url).hostname)); }
  function thumb(id) { return `https://i.ytimg.com/vi/${encodeURIComponent(id)}/hqdefault.jpg`; }
  function timeSeconds(value = '') { const parts = String(value).split(':').map(Number); return parts.reduce((acc, part) => acc * 60 + (Number.isFinite(part) ? part : 0), 0); }
  function durationText(value = '') { if (/^P/.test(value)) { const m = value.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/) || []; const h = Number(m[1] || 0), min = Number(m[2] || 0), sec = Number(m[3] || 0); return h ? `${h}:${String(min).padStart(2,'0')}:${String(sec).padStart(2,'0')}` : `${min}:${String(sec).padStart(2,'0')}`; } return value || '—'; }
  function notify(message) { const node = document.querySelector('#toast'); node.textContent = message; node.classList.add('show'); clearTimeout(toastTimer); toastTimer = setTimeout(() => node.classList.remove('show'), 2600); }
  function setPage(page) { currentPage = page; activeResult = page === 'result' ? activeResult : null; render(); document.querySelector('#sidebar').classList.remove('open'); window.scrollTo(0, 0); }
  function titleFor(page) { return ({overview:'Overview',analyze:'Analyze Video',library:'My Library',notes:'Notes',result:'Video Analysis'})[page] || 'Overview'; }

  function render() {
    pageTitle.textContent = titleFor(currentPage);
    document.querySelectorAll('.nav-item[data-page]').forEach(button => button.classList.toggle('active', button.dataset.page === (currentPage === 'result' ? 'library' : currentPage)));
    if (currentPage === 'overview') content.innerHTML = overviewPage();
    if (currentPage === 'analyze') content.innerHTML = analyzePage();
    if (currentPage === 'library') content.innerHTML = libraryPage();
    if (currentPage === 'notes') content.innerHTML = notesPage();
    if (currentPage === 'result') content.innerHTML = resultPage();
    bindPageEvents();
  }

  function overviewPage() {
    const topics = saved.reduce((sum, item) => sum + (item.keyTopics?.length || 0), 0);
    const notes = saved.reduce((sum, item) => sum + (item.notes?.length || 0), 0);
    const seconds = saved.reduce((sum, item) => sum + timeSeconds(item.duration), 0);
    const hours = Math.floor(seconds / 3600), mins = Math.floor((seconds % 3600) / 60);
    const recent = [...saved].reverse().slice(0, 3);
    const now = new Date();
    const activity = [...Array(7)].map((_, index) => {
      const date = new Date(now); date.setDate(now.getDate() - (6 - index));
      const count = saved.filter(item => item.savedAt && new Date(item.savedAt).toDateString() === date.toDateString()).length;
      return { label: date.toLocaleDateString(undefined, { weekday: 'short' }), count };
    });
    return `<section class="hero"><div><span class="eyebrow">✧ AI learning assistant</span><h1>Turn videos into <span class="accent">knowledge.</span></h1><p>Your AI-powered workspace for understanding, organizing, and revisiting what you learn.</p></div><button class="primary-btn" data-go="analyze">▣ &nbsp;Analyze New Video</button></section>
      <section class="stats"><article class="stat-card"><div class="stat-label">Videos analyzed <span class="stat-icon">▣</span></div><div class="stat-value">${saved.length}</div><div class="stat-hint">In your library</div></article><article class="stat-card"><div class="stat-label">Video learning time <span class="stat-icon">◷</span></div><div class="stat-value">${hours ? `${hours}h ${mins}m` : `${mins}m`}</div><div class="stat-hint">Across analyzed videos</div></article><article class="stat-card"><div class="stat-label">Notes generated <span class="stat-icon">▧</span></div><div class="stat-value">${notes}</div><div class="stat-hint">Timestamped study notes</div></article><article class="stat-card"><div class="stat-label">Topics discovered <span class="stat-icon">#</span></div><div class="stat-value">${topics}</div><div class="stat-hint">Across your library</div></article></section>
      <section class="dashboard-grid"><article class="panel"><h3>Learning activity</h3><div class="panel-sub">Videos analyzed over the last 7 days</div>${saved.length ? `<div class="activity-chart" aria-label="Videos analyzed each day over the last 7 days">${activity.map(day=>`<div class="day"><i class="bar" style="height:${day.count ? Math.min(100,Math.max(12,day.count*30)) : 2}%;opacity:${day.count?1:.15}"></i><span class="day-label">${esc(day.label)}</span></div>`).join('')}</div>` : `<div class="empty-state" style="margin-top:18px;padding:27px"><div class="empty-icon">▥</div><h2>Your learning activity will appear here</h2><p>Analyze your first video to start building a learning history.</p><button class="primary-btn" data-go="analyze">Analyze a video</button></div>`}</article>
      <article class="panel"><h3>Continue learning</h3><div class="panel-sub">Pick up where you left off</div>${recent.length ? `<div class="recent-list">${recent.map(item=>`<div class="recent-row" data-open="${esc(item.id)}"><img src="${thumb(item.id)}" alt="" onerror="this.style.visibility='hidden'"><div><b>${esc(item.title)}</b><small>${esc(item.channel || 'YouTube')} · ${esc(durationText(item.duration))}</small></div></div>`).join('')}</div>` : `<div class="overview-empty">Your analyzed videos will show up here.</div>`}</article></section>
      <div class="section-heading"><h2>Recently analyzed</h2><button class="text-btn" data-go="library">View library →</button></div>${recent.length ? `<section class="video-grid">${recent.map(videoCard).join('')}</section>` : `<div class="overview-empty">Your library is ready when you are.</div>`}`;
  }

  function analyzePage() { return `<div class="analyze-header"><div><h1>Analyze a video</h1><p>Generate a structured learning guide from any YouTube video.</p></div><span class="not-available">Uses your connected AI services</span></div><div class="analyze-form-wrap"><form id="analyze-form" class="analyze-form"><label for="youtube-url">YouTube URL</label><div class="url-row"><div class="url-input"><span>↗</span><input id="youtube-url" name="url" type="url" placeholder="Paste a YouTube video link to generate your learning guide…" required /></div><button class="primary-btn" type="submit">✧ &nbsp;Analyze Video</button></div><div class="examples"><span>Try an example:</span><button class="chip" type="button" data-example="https://www.youtube.com/watch?v=aircAruvnKk">Neural networks</button><button class="chip" type="button" data-example="https://www.youtube.com/watch?v=eMlx5fFNoYc">Transformers</button><button class="chip" type="button" data-example="https://www.youtube.com/watch?v=T-D1OfcDW1M">RAG explained</button></div><div class="error-box" id="analyze-error"></div></form><div class="loading" id="loading"><b>Analyzing your video…</b><div class="panel-sub">Fetching metadata and transcript, then generating your learning guide. This can take a minute.</div><div class="loading-bar"><i></i></div></div></div><div id="inline-result"></div>`; }

  function videoCard(item) { return `<article class="video-card" data-open="${esc(item.id)}"><div class="thumb-wrap"><img src="${thumb(item.id)}" alt="Thumbnail for ${esc(item.title)}" onerror="this.src='https://placehold.co/640x360/eff1f7/68738b?text=YouTube+video'"><span class="thumb-duration">${esc(durationText(item.duration))}</span></div><div class="video-info"><div class="badges"><span class="badge">Completed</span><span># ${item.keyTopics?.length || 0} topics</span></div><h3>${esc(item.title)}</h3><p>${esc(item.channel || 'YouTube')}</p><div class="card-footer"><span>◷ &nbsp;${esc(item.savedAt ? new Date(item.savedAt).toLocaleDateString(undefined,{month:'short',day:'numeric',year:'numeric'}) : 'Saved')}</span><button class="secondary-btn" data-open="${esc(item.id)}">Open analysis</button></div></div></article>`; }
  function libraryPage() { return `<div class="section-heading" style="margin-top:0"><div><h1 style="font:800 27px Manrope;margin:0 0 5px">My Library</h1><div class="panel-sub">${saved.length} analyzed ${saved.length === 1 ? 'video' : 'videos'} in your workspace</div></div><button class="primary-btn" data-go="analyze">＋ Analyze video</button></div>${saved.length ? `<section class="video-grid">${[...saved].reverse().map(videoCard).join('')}</section>` : `<div class="empty-state"><div class="empty-icon">▤</div><h2>Your library is empty</h2><p>Analyze a YouTube video to save its summary, topics, transcript, and study notes here.</p><button class="primary-btn" data-go="analyze">Analyze your first video</button></div>`}`; }
  function notesPage() { const entries = saved.flatMap(video => (video.notes || []).map(note => ({...note, video}))); return `<div class="section-heading" style="margin-top:0"><div><h1 style="font:800 27px Manrope;margin:0 0 5px">Study Notes</h1><div class="panel-sub">Timestamped notes from your analyzed videos</div></div></div>${entries.length ? `<div class="analysis-card"><div class="note-list">${entries.map(note=>`<article class="note-item"><a class="timestamp" target="_blank" rel="noopener" href="https://www.youtube.com/watch?v=${encodeURIComponent(note.video.id)}&t=${timeSeconds(note.timestamp)}s">${esc(note.timestamp || '0:00')}</a><div><h4>${esc(note.title || 'Study note')} <button class="note-video" type="button" data-open-notes="${esc(note.video.id)}">· ${esc(note.video.title)}</button></h4><p>${esc(note.notes || '')}</p></div></article>`).join('')}</div></div>` : `<div class="empty-state"><div class="empty-icon">▧</div><h2>No notes yet</h2><p>Timestamped study notes are generated when a video is analyzed.</p><button class="primary-btn" data-go="analyze">Analyze a video</button></div>`}`; }

  function resultPage() {
    if (!activeResult) return libraryPage();
    const item=activeResult, topics=item.keyTopics || [], notes=item.notes || [];
    let body='';
    if(resultTab==='summary') body=`<article class="analysis-card"><h3>Overview <span class="panel-sub"> · AI generated</span> <button class="text-btn" style="float:right" data-copy="summary">Copy</button></h3><div class="summary-copy">${esc(item.summary || 'No summary was returned.')}</div></article><article class="analysis-card"><h3>Key takeaways</h3>${topics.length?`<div class="topic-list">${topics.map((topic,i)=>`<div class="topic-item"><span class="stat-icon">${i+1}</span><span>${esc(topic.topic || topic.title || topic)}</span>${topic.timestamp?`<a class="timestamp" target="_blank" rel="noopener" href="https://www.youtube.com/watch?v=${encodeURIComponent(item.id)}&t=${timeSeconds(topic.timestamp)}s">${esc(topic.timestamp)}</a>`:''}</div>`).join('')}</div>`:'<p class="panel-sub">No key topics were returned.</p>'}</article>`;
    if(resultTab==='notes') body=`<article class="analysis-card"><h3>Timestamped study notes</h3>${notes.length?`<div class="note-list">${notes.map(note=>`<article class="note-item"><a class="timestamp" target="_blank" rel="noopener" href="https://www.youtube.com/watch?v=${encodeURIComponent(item.id)}&t=${timeSeconds(note.timestamp)}s">${esc(note.timestamp || '0:00')}</a><div><h4>${esc(note.title || 'Study note')}</h4><p>${esc(note.notes || '')}</p></div></article>`).join('')}</div>`:'<p class="panel-sub">No timestamped notes were returned.</p>'}</article>`;
    if(resultTab==='topics') body=`<article class="analysis-card"><h3>Key topics</h3>${topics.length?`<div class="topic-list">${topics.map(topic=>`<div class="topic-item">${topic.timestamp?`<a class="timestamp" target="_blank" rel="noopener" href="https://www.youtube.com/watch?v=${encodeURIComponent(item.id)}&t=${timeSeconds(topic.timestamp)}s">${esc(topic.timestamp)}</a>`:''}<span>${esc(topic.topic || topic.title || topic)}</span></div>`).join('')}</div>`:'<p class="panel-sub">No key topics were returned.</p>'}</article>`;
    if(resultTab==='transcript') body=`<article class="analysis-card"><h3>Transcript</h3><div class="transcript-copy">${esc(item.transcript || 'No transcript was returned.')}</div></article>`;
    const videoHref=`https://www.youtube.com/watch?v=${encodeURIComponent(item.id)}`;
    return `<div class="result-top"><div><button class="text-btn" data-go="library">← Back to library</button><h1>${esc(item.title)}</h1><div class="result-meta"><span>${esc(item.channel || 'YouTube')}</span><span>◷ ${esc(durationText(item.duration))}</span><span>Analyzed ${esc(item.savedAt?new Date(item.savedAt).toLocaleDateString(): 'recently')}</span><a href="${videoHref}" target="_blank" rel="noopener">YouTube ↗</a></div></div><div class="result-actions"><button class="secondary-btn" data-copy="summary">▢ Copy summary</button></div></div><div class="result-layout"><div><div class="tabbar">${[['summary','Summary'],['notes','Detailed Notes'],['topics','Key Topics'],['transcript','Transcript']].map(([key,label])=>`<button class="tab ${resultTab===key?'active':''}" data-tab="${key}">${label}</button>`).join('')}</div>${body}</div><aside class="side-column"><div><a href="${videoHref}" target="_blank" rel="noopener"><img class="side-video" src="${thumb(item.id)}" alt="Video thumbnail"></a></div><article class="analysis-card side-panel"><h3>Learning insights</h3><div class="insight-row"><span>Video length</span><b>${esc(durationText(item.duration))}</b></div><div class="insight-row"><span>Key topics</span><b>${topics.length}</b></div><div class="insight-row"><span>Study notes</span><b>${notes.length}</b></div><div class="insight-row"><span>Transcript</span><b>${item.transcript?'Available':'Unavailable'}</b></div><div style="margin-top:10px"><button class="secondary-btn" data-copy="transcript" style="width:100%">Copy transcript</button></div></article></aside></div>`;
  }

  function bindPageEvents() {
    content.querySelectorAll('[data-go]').forEach(node=>node.addEventListener('click',()=>setPage(node.dataset.go)));
    content.querySelectorAll('[data-open-notes]').forEach(node=>node.addEventListener('click',()=>{const item=saved.find(video=>video.id===node.dataset.openNotes);if(item){activeResult=item;resultTab='notes';setPage('result');}}));
    content.querySelectorAll('[data-open]').forEach(node=>node.addEventListener('click',event=>{if(event.target.closest('button') && !event.target.closest('[data-open]')) return; const item=saved.find(video=>video.id===node.dataset.open); if(item){activeResult=item;resultTab='summary';setPage('result');}}));
    content.querySelectorAll('[data-tab]').forEach(node=>node.addEventListener('click',()=>{resultTab=node.dataset.tab;render();}));
    content.querySelectorAll('[data-example]').forEach(node=>node.addEventListener('click',()=>{document.querySelector('#youtube-url').value=node.dataset.example;}));
    content.querySelectorAll('[data-copy]').forEach(node=>node.addEventListener('click',async()=>{const value=node.dataset.copy==='transcript'?activeResult?.transcript:activeResult?.summary;if(!value)return;try{await navigator.clipboard.writeText(value);notify(`${node.dataset.copy==='transcript'?'Transcript':'Summary'} copied`);}catch{notify('Clipboard access is unavailable in this browser');}}));
    const form=document.querySelector('#analyze-form'); if(form) form.addEventListener('submit',analyze);
  }

  async function analyze(event) {
    event.preventDefault(); const url=document.querySelector('#youtube-url').value.trim(), error=document.querySelector('#analyze-error'), loading=document.querySelector('#loading');
    error.style.display='none';
    try { if(!validYoutube(url)) throw new Error('Enter a valid YouTube video URL, such as youtube.com/watch?v=… or youtu.be/…'); }
    catch(e){error.textContent=e.message;error.style.display='block';return;}
    loading.classList.add('show');document.querySelector('#connection-status').textContent='Connecting';document.querySelector('.connection i').style.background='#efb34a';
    try {
      const response=await fetch(`${API_BASE}/api/video/analyze?url=${encodeURIComponent(url)}`,{headers:{Accept:'application/json'}});
      if(!response.ok){let message=`Analysis failed (${response.status})`;try{const data=await response.json();message=data.message||data.error||data.detail?.error||message;}catch{}throw new Error(message);}
      const data=await response.json(), id=videoId(url);
      if(!data || !data.title) throw new Error('The API returned an incomplete analysis response.');
      const item={id,title:data.title,channel:data.channel||'',duration:durationText(data.duration),transcript:data.transcript||'',summary:data.summary||'',keyTopics:Array.isArray(data.keyTopics)?data.keyTopics:[],notes:Array.isArray(data.notes)?data.notes:[],savedAt:new Date().toISOString()};
      const existing=saved.findIndex(video=>video.id===id); if(existing>=0)saved.splice(existing,1); saved.push(item);persist();activeResult=item;resultTab='summary';
      document.querySelector('#connection-status').textContent='Connected';document.querySelector('.connection i').style.background='#11b981';
      currentPage='result';render();window.scrollTo(0,0);
    } catch(errorValue) {
      error.textContent=`${errorValue.message}${errorValue instanceof TypeError?` Check that the backend is running at ${API_BASE} and allows browser requests.`:''}`;error.style.display='block';document.querySelector('#connection-status').textContent='Unavailable';document.querySelector('.connection i').style.background='#eb6b6b';
    } finally {loading.classList.remove('show');}
  }

  document.querySelectorAll('.nav-item[data-page]').forEach(button=>button.addEventListener('click',()=>setPage(button.dataset.page)));
  document.querySelector('#menu-toggle').addEventListener('click',()=>document.querySelector('#sidebar').classList.toggle('open'));
  const search=document.querySelector('#global-search');
  search.addEventListener('keydown',event=>{if(event.key==='Enter'){const query=search.value.trim().toLowerCase();if(!query)return;setPage('library');const matches=saved.filter(video=>`${video.title} ${video.channel} ${video.summary} ${video.keyTopics.map(topic=>topic.topic||'').join(' ')}`.toLowerCase().includes(query));content.innerHTML=`<div class="section-heading" style="margin-top:0"><div><h1 style="font:800 27px Manrope;margin:0 0 5px">Search results</h1><div class="panel-sub">${matches.length} matches for “${esc(search.value.trim())}”</div></div></div>${matches.length?`<section class="video-grid">${matches.map(videoCard).join('')}</section>`:`<div class="empty-state"><h2>No matches found</h2><p>Try another title, channel, topic, or summary phrase.</p></div>`}`;bindPageEvents();}});
  render();
})();

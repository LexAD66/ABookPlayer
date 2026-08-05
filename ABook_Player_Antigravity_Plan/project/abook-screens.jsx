// abook-screens.jsx — All screens for ABook Player, parameterized by theme.
// Two themes: 'library' (warm/literary) and 'stage' (dark/cinematic).
// Each screen is a 412x892 phone screen body (no status/nav bar — those
// come from AndroidDevice in the canvas composition).

// ─────────────────────────────────────────────────────────
// Theme tokens
// ─────────────────────────────────────────────────────────
const LIBRARY_THEME = {
  name: 'library',
  bg: '#f1eadb',
  surface: '#faf6ec',
  surfaceAlt: '#e8dfc8',
  surfaceDeep: '#dccfae',
  ink: '#2c241a',
  inkDim: '#6e6354',
  inkLight: '#8e8474',
  accent: '#b4451e',
  accentSoft: 'rgba(180,69,30,0.12)',
  border: 'rgba(44,36,26,0.10)',
  borderStrong: 'rgba(44,36,26,0.2)',
  sans: '"Inter", "Helvetica Neue", Arial, sans-serif',
  serif: '"Cormorant Garamond", "EB Garamond", Georgia, serif',
  mono: '"JetBrains Mono", "IBM Plex Mono", ui-monospace, monospace',
  shadow: '0 1px 2px rgba(44,36,26,0.06), 0 4px 18px rgba(44,36,26,0.06)'
};

const STAGE_THEME = {
  name: 'stage',
  bg: '#0a0a0c',
  surface: '#13131a',
  surfaceAlt: '#1d1d26',
  surfaceDeep: '#2a2a36',
  ink: '#f2efe7',
  inkDim: '#9d9aa0',
  inkLight: '#5e5b65',
  accent: '#f5b86e',
  accentSoft: 'rgba(245,184,110,0.14)',
  border: 'rgba(255,255,255,0.06)',
  borderStrong: 'rgba(255,255,255,0.14)',
  sans: '"Inter", "Helvetica Neue", Arial, sans-serif',
  serif: '"Cormorant Garamond", "EB Garamond", Georgia, serif',
  mono: '"JetBrains Mono", "IBM Plex Mono", ui-monospace, monospace',
  shadow: '0 1px 2px rgba(0,0,0,0.3), 0 8px 28px rgba(0,0,0,0.4)'
};

const THEMES = { library: LIBRARY_THEME, stage: STAGE_THEME };

// ─────────────────────────────────────────────────────────
// Inline SVG icons (stroke-currentColor)
// ─────────────────────────────────────────────────────────
const Icon = ({ d, size = 20, fill, stroke = 'currentColor', sw = 1.6, style }) =>
<svg width={size} height={size} viewBox="0 0 24 24" fill={fill || 'none'} stroke={stroke}
strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" style={style}>
    <path d={d} />
  </svg>;

const Icons = {
  menu: <Icon d="M3 6h18M3 12h18M3 18h18" />,
  search: <Icon d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zm6-2l4 4" />,
  more: <Icon d="M12 5h.01M12 12h.01M12 19h.01" sw={3} />,
  grid: <Icon d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z" />,
  list: <Icon d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />,
  add: <Icon d="M5 4h10l4 4v12a1 1 0 0 1-1 1H5zM15 4v4h4M11 12v6M8 15h6" />,
  refresh: <Icon d="M3 12a9 9 0 0 1 15-6.7L21 8M21 3v5h-5M21 12a9 9 0 0 1-15 6.7L3 16M3 21v-5h5" />,
  back: <Icon d="M15 18l-6-6 6-6" />,
  play: <Icon d="M7 4l13 8L7 20z" fill="currentColor" stroke="none" />,
  pause: <Icon d="M7 4h4v16H7zM13 4h4v16h-4z" fill="currentColor" stroke="none" />,
  prev: <Icon d="M19 5L9 12l10 7zM6 5v14" fill="currentColor" stroke="none" />,
  next: <Icon d="M5 5l10 7L5 19zM18 5v14" fill="currentColor" stroke="none" />,
  ff10: <Icon d="M3 12a9 9 0 1 0 3-6.7M3 3v5h5" />,
  rw10: <Icon d="M21 12a9 9 0 1 1-3-6.7M21 3v5h-5" />,
  clock: <Icon d="M12 7v5l3 2M12 22a10 10 0 1 1 0-20 10 10 0 0 1 0 20z" />,
  cast: <Icon d="M2 16v4h4M2 12c5 0 9 4 9 9M2 8c8 0 13 5 13 13M21 4H3a1 1 0 0 0-1 1v3" />,
  speed: <Icon d="M12 12V6M12 12l5 3M3 12a9 9 0 1 1 9 9" />,
  bookmark: <Icon d="M5 3h14v18l-7-4-7 4z" />,
  lock: <Icon d="M5 11h14v9H5zM8 11V7a4 4 0 0 1 8 0v4" />,
  people: <Icon d="M3 21a6 6 0 0 1 12 0M21 21a4 4 0 0 0-6-3.5M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM17 11a3 3 0 1 0 0-6" />,
  chapter: <Icon d="M4 6h16M4 12h16M4 18h10" />,
  volume: <Icon d="M4 9v6h4l5 4V5L8 9zM16 8a5 5 0 0 1 0 8M19 5a9 9 0 0 1 0 14" />,
  zzz: <Icon d="M5 5h6L5 12h6M13 12h6l-6 8h6" />,
  close: <Icon d="M5 5l14 14M19 5L5 19" />,
  check: <Icon d="M5 12l5 5 9-11" sw={2.2} />,
  star: <Icon d="M12 3l2.5 6 6.5.6-5 4.5 1.5 6.4L12 17l-5.5 3.5L8 14.1 3 9.6l6.5-.6z" />,
  flame: <Icon d="M12 3c0 5-5 5-5 10a5 5 0 0 0 10 0c0-3-2-4-2-7 0 0-1 1-3 1-1.5 0-0-4 0-4z" />,
  graph: <Icon d="M3 18l4-8 5 4 5-10 4 6" />,
  quote: <Icon d="M7 9h3v6H4V11a4 4 0 0 1 3-4M17 9h3v6h-6V11a4 4 0 0 1 3-4" />
};

// ─────────────────────────────────────────────────────────
// Shared chrome primitives
// ─────────────────────────────────────────────────────────
function PhoneShell({ theme, children, dark, style, padded = false }) {
  return (
    <div style={{
      width: '100%', height: '100%',
      background: theme.bg, color: theme.ink,
      fontFamily: theme.sans, fontSize: 14, lineHeight: 1.4,
      display: 'flex', flexDirection: 'column',
      ...(padded ? { padding: '0 16px' } : {}),
      ...style
    }}>{children}</div>);

}

// App bar styled per theme
function AppBar({ theme, leading = 'menu', title, brand, right, sub, dark }) {
  const isLib = theme.name === 'library';
  return (
    <div style={{
      padding: '14px 12px 10px',
      borderBottom: `1px solid ${theme.border}`,
      background: theme.surface,
      display: 'flex', alignItems: 'center', gap: 8,
      color: theme.ink
    }}>
      <button style={{ background: 'none', border: 'none', color: theme.ink, padding: 8, display: 'flex', cursor: 'pointer' }}>
        {leading === 'back' ? Icons.back : Icons.menu}
      </button>
      <div style={{ flex: 1, minWidth: 0 }}>
        {brand &&
        <div style={{
          fontFamily: isLib ? theme.serif : theme.sans,
          fontSize: isLib ? 22 : 17, fontWeight: isLib ? 600 : 700,
          letterSpacing: isLib ? -0.2 : 0.4,
          fontStyle: isLib ? 'italic' : 'normal',
          color: theme.ink,
          textTransform: isLib ? 'none' : 'uppercase'
        }}>{brand}</div>
        }
        {title &&
        <div style={{
          fontFamily: theme.sans, fontSize: 16, fontWeight: 500, color: theme.ink,
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'
        }}>{title}</div>
        }
        {sub && <div style={{ fontSize: 11, color: theme.inkDim, marginTop: 2 }}>{sub}</div>}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: theme.ink }}>
        {right}
      </div>
    </div>);

}

// ─────────────────────────────────────────────────────────
// SCREEN 1 — Library
// ─────────────────────────────────────────────────────────
function ScreenLibrary({ theme }) {
  const isLib = theme.name === 'library';
  const tabs = ['ALL', 'NEW', 'STARTED', 'FINISHED'];
  const groups = [
  { author: 'Agatha Christie', count: 7, books: ['vicarage', 'paddington'] },
  { author: 'J. R. R. Tolkien', count: 6, books: ['hobbit', 'fellowship'] },
  { author: 'Alexandre Dumas', count: 2, books: ['monte'] }];

  return (
    <PhoneShell theme={theme}>
      {/* Top bar with folder selector */}
      <div style={{ padding: '12px 12px 8px', background: theme.surface, borderBottom: `1px solid ${theme.border}` }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'auto minmax(0,1fr) auto auto auto auto', alignItems: 'center', gap: 2, color: theme.ink }}>
          <button style={{ background: 'none', border: 'none', color: theme.ink, padding: 6, display: 'flex' }}>{Icons.menu}</button>
          <div style={{ display: 'flex', alignItems: 'center', gap: 3, minWidth: 0, overflow: 'hidden' }}>
            <span style={{
              fontFamily: isLib ? theme.serif : theme.sans,
              fontSize: isLib ? 19 : 15, fontWeight: 600,
              fontStyle: isLib ? 'italic' : 'normal',
              color: theme.ink,
              whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', minWidth: 0
            }}>…audiobooks</span>
            <Icon d="M6 9l6 6 6-6" size={12} />
          </div>
          <button style={{ background: 'none', border: 'none', color: theme.ink, padding: 5, display: 'flex' }}>{Icons.search}</button>
          <button style={{ background: 'none', border: 'none', color: theme.accent, padding: 5, display: 'flex' }}>{Icons.grid}</button>
          <button style={{ background: 'none', border: 'none', color: theme.ink, padding: 5, display: 'flex' }}>{Icons.add}</button>
          <button style={{ background: 'none', border: 'none', color: theme.ink, padding: 5, display: 'flex' }}>{Icons.more}</button>
        </div>
        {/* Tabs */}
        <div style={{ display: 'flex', gap: 18, marginTop: 8, paddingTop: 6 }}>
          {tabs.map((t, i) =>
          <div key={t} style={{
            fontSize: 12, letterSpacing: 1.2, fontWeight: 600,
            color: i === 1 ? theme.accent : theme.inkDim,
            borderBottom: i === 1 ? `2px solid ${theme.accent}` : '2px solid transparent',
            paddingBottom: 6
          }}>{t}</div>
          )}
        </div>
      </div>

      {/* Author groups */}
      <div style={{ flex: 1, overflow: 'auto' }}>
        {groups.map((g, gi) =>
        <div key={g.author} style={{ marginTop: gi === 0 ? 16 : 22 }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0,1fr) auto', alignItems: 'baseline', padding: '0 16px 10px', gap: 8 }}>
              <div style={{
              fontFamily: isLib ? theme.serif : theme.sans,
              fontSize: isLib ? 22 : 16, fontWeight: isLib ? 500 : 600,
              color: theme.ink,
              fontStyle: isLib ? 'italic' : 'normal',
              letterSpacing: isLib ? -0.2 : 0,
              whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', minWidth: 0
            }}>{g.author}</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, color: theme.inkDim, fontSize: 11 }}>
                <span>{g.count}</span>
                <Icon d="M9 6l6 6-6 6" size={12} />
              </div>
            </div>
            <div style={{ display: 'flex', gap: 14, padding: '0 16px', overflowX: 'auto' }}>
              {g.books.map((b, bi) =>
            <div key={b} style={{ flexShrink: 0, width: 142 }}>
                  <div style={{
                width: '100%', aspectRatio: '3/4.4',
                borderRadius: isLib ? 2 : 4,
                overflow: 'hidden',
                boxShadow: isLib ?
                '0 2px 4px rgba(44,36,26,0.15), 0 8px 24px rgba(44,36,26,0.08)' :
                '0 4px 12px rgba(0,0,0,0.4), 0 0 0 1px rgba(255,255,255,0.04)'
              }}>
                    <Cover id={b} />
                  </div>
                  <div style={{
                fontSize: 13, marginTop: 8, color: theme.ink, lineHeight: 1.3,
                overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical'
              }}>
                    {bookTitle(b)}
                  </div>
                </div>
            )}
            </div>
          </div>
        )}
        <div style={{ height: 60 }} />
      </div>
    </PhoneShell>);

}

const bookTitle = (id) => ({
  hound: 'The Hound of the Baskervilles',
  monte: 'The Count of Monte Cristo',
  hobbit: '1. The Hobbit',
  fellowship: '2. The Fellowship of the Ring',
  threemen: 'Three Men in a Boat',
  vicarage: 'The Murder at the Vicarage',
  paddington: '4.50 from Paddington'
})[id];

// ─────────────────────────────────────────────────────────
// SCREEN 2 — Now Playing
// ─────────────────────────────────────────────────────────
function ScreenPlayer({ theme, bookId = 'monte' }) {
  const isLib = theme.name === 'library';
  return (
    <PhoneShell theme={theme}>
      {/* Ambient bg layer (stage theme: blurred cover) */}
      {!isLib &&
      <div style={{
        position: 'absolute', inset: 0, opacity: 0.4, filter: 'blur(40px) saturate(1.4)',
        transform: 'scale(1.4)', pointerEvents: 'none'
      }}>
          <Cover id={bookId} />
        </div>
      }
      <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
        {/* Top app bar */}
        <div style={{ display: 'flex', alignItems: 'center', padding: '8px 8px', color: theme.ink }}>
          <button style={ibtn(theme)}>{Icons.menu}</button>
          <div style={{
            flex: 1, textAlign: 'left', paddingLeft: 4,
            fontFamily: isLib ? theme.serif : theme.sans,
            fontSize: isLib ? 22 : 16, fontWeight: isLib ? 600 : 700,
            fontStyle: isLib ? 'italic' : 'normal',
            letterSpacing: isLib ? -0.2 : 0.4,
            textTransform: isLib ? 'none' : 'uppercase'
          }}>ABook</div>
          <button style={{ ...ibtn(theme), color: theme.accent, fontFamily: theme.mono, fontSize: 11, fontWeight: 700 }}>ID3</button>
          <button style={ibtn(theme)}>{Icons.pause}</button>
          <button style={ibtn(theme)}>{Icons.refresh}</button>
          <button style={ibtn(theme)}>{Icons.cast}</button>
          <button style={ibtn(theme)}>{Icons.more}</button>
        </div>

        {/* Quick toolbar */}
        <div style={{ display: 'flex', alignItems: 'center', padding: '2px 12px 8px', color: theme.ink, gap: 6 }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, color: theme.accent, marginRight: 'auto' }}>
            <div style={{ position: 'relative' }}>
              {Icons.clock}
              <span style={{ position: 'absolute', top: -4, right: -10, fontSize: 8, fontWeight: 600, fontFamily: theme.mono }}>Zzz</span>
            </div>
            <span style={{ fontSize: 11, color: theme.accent, fontFamily: theme.mono }}>10:00</span>
          </div>
          <button style={{ ...ibtn(theme), color: theme.accent }}>{Icons.volume}</button>
          <button style={ibtn(theme)}>{Icons.chapter}</button>
          <button style={{ ...ibtn(theme), fontWeight: 700, fontSize: 15, color: theme.accent, fontFamily: theme.mono }}>1.0x</button>
          <button style={ibtn(theme)}>{Icons.people}</button>
          <button style={ibtn(theme)}>{Icons.bookmark}</button>
          <button style={ibtn(theme)}>{Icons.lock}</button>
        </div>

        {/* Title block */}
        <div style={{ textAlign: 'center', padding: '0 16px 10px' }}>
          <div style={{
            fontFamily: isLib ? theme.serif : theme.sans,
            fontSize: isLib ? 24 : 18, fontWeight: isLib ? 600 : 600,
            fontStyle: isLib ? 'italic' : 'normal',
            color: theme.ink, lineHeight: 1.15, letterSpacing: isLib ? -0.3 : 0
          }}>The Count of Monte Cristo</div>
          {/* book progress bar */}
          <div style={{ height: 3, background: theme.border, marginTop: 10, borderRadius: 2, overflow: 'hidden' }}>
            <div style={{ width: '24%', height: '100%', background: theme.accent }} />
          </div>
          <div style={{
            display: 'flex', justifyContent: 'center', gap: 16, marginTop: 6,
            fontSize: 11, color: theme.inkDim, fontFamily: theme.mono
          }}>
            <span>Read <b style={{ color: theme.ink }}>11:03:47</b> of 46:56:40</span>
            <span style={{ color: theme.accent }}>24%</span>
            <span>Left 35:52:53</span>
          </div>
        </div>

        {/* Cover artwork — large hero */}
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '4px 16px' }}>
          <div style={{
            width: '100%', maxWidth: 340, aspectRatio: '1',
            borderRadius: isLib ? 4 : 10, overflow: 'hidden',
            position: 'relative',
            boxShadow: isLib ?
            '0 8px 24px rgba(44,36,26,0.2), 0 2px 6px rgba(44,36,26,0.15)' :
            '0 16px 40px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.06), 0 0 60px ' + theme.accentSoft
          }}>
            <Cover id={bookId} />
            {/* Center play overlay */}
            <div style={{
              position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%,-50%)',
              width: 78, height: 78, borderRadius: '50%',
              background: isLib ? 'rgba(250,246,236,0.92)' : 'rgba(20,20,26,0.7)',
              backdropFilter: 'blur(10px)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: isLib ? theme.ink : theme.accent,
              boxShadow: '0 4px 18px rgba(0,0,0,0.25)'
            }}>
              <Icon d="M8 5l12 7-12 7z" size={28} fill="currentColor" stroke="none" />
            </div>
          </div>
        </div>

        {/* Chapter row */}
        <div style={{ padding: '10px 16px 6px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <button style={{ ...ibtn(theme), padding: 8 }}>{Icons.prev}</button>
            <div style={{ flex: 1, textAlign: 'center', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, color: theme.ink, fontSize: 13 }}>
              <span>The Count of Monte Cristo · Part 11</span>
              <Icon d="M6 9l6 6 6-6" size={12} />
            </div>
            <button style={{ ...ibtn(theme), padding: 8 }}>{Icons.next}</button>
          </div>
          {/* track scrubber */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 8, color: theme.inkDim, fontFamily: theme.mono, fontSize: 11 }}>
            <span>35:56</span>
            <div style={{ flex: 1, height: 4, background: theme.border, borderRadius: 2, position: 'relative' }}>
              <div style={{ width: '46%', height: '100%', background: theme.accent, borderRadius: 2 }} />
              <div style={{ position: 'absolute', top: '50%', left: '46%', transform: 'translate(-50%,-50%)',
                width: 12, height: 12, borderRadius: '50%', background: theme.accent,
                boxShadow: '0 0 0 4px ' + theme.accentSoft }} />
            </div>
            <span>-41:59</span>
          </div>
        </div>

        {/* Big transport */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-around', padding: '10px 8px 18px', color: theme.ink }}>
          {[
          { label: '1 m', icon: <Icon d="M5 5L11 12L5 19zM12 5L18 12L12 19z" size={28} fill="currentColor" stroke="none" style={{ transform: 'scaleX(-1)' }} /> },
          { label: '10 s', icon: <Icon d="M9 5L17 12L9 19z" size={28} fill="currentColor" stroke="none" style={{ transform: 'scaleX(-1)' }} /> },
          { label: '10 s', icon: <Icon d="M9 5L17 12L9 19z" size={28} fill="currentColor" stroke="none" /> },
          { label: '1 m', icon: <Icon d="M5 5L11 12L5 19zM12 5L18 12L12 19z" size={28} fill="currentColor" stroke="none" /> }].
          map((b, i) =>
          <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
              {b.icon}
              <span style={{ fontSize: 11, color: theme.inkDim, fontFamily: theme.mono }}>{b.label}</span>
            </div>
          )}
        </div>
      </div>
    </PhoneShell>);

}

const ibtn = (theme) => ({
  background: 'none', border: 'none', color: theme.ink, padding: 6,
  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer'
});

// ─────────────────────────────────────────────────────────
// SCREEN 3 — Chapters dialog
// ─────────────────────────────────────────────────────────
function ScreenChapters({ theme }) {
  const isLib = theme.name === 'library';
  const chapters = [
  { n: 6, title: 'Baskerville Hall', len: '19:26' },
  { n: 7, title: 'The Stapletons of Merripit House', len: '27:17' },
  { n: 8, title: 'First Report of Dr. Watson', len: '14:47' },
  { n: 9, title: '[Second Report of Dr. Watson] The Light Upon the Moor, Baskerville Hall, October 15th', len: '34:24', active: true },
  { n: 10, title: 'Extract From the Diary of Dr. Watson', len: '19:44' },
  { n: 11, title: 'The Man on the Tor', len: '24:08' },
  { n: 12, title: 'Death on the Moor', len: '22:57' },
  { n: 13, title: 'Fixing the Nets', len: '22:04' },
  { n: 14, title: 'The Hound of the Baskervilles', len: '22:33' },
  { n: 15, title: 'A Retrospection', len: '22:11' }];

  return (
    <PhoneShell theme={theme}>
      {/* Faded background of the player (suggests modal) */}
      <div style={{ position: 'absolute', inset: 0, opacity: isLib ? 0.5 : 0.25, pointerEvents: 'none', filter: 'blur(10px)' }}>
        <Cover id="hound" />
      </div>
      <div style={{ position: 'absolute', inset: 0, background: isLib ? 'rgba(241,234,219,0.7)' : 'rgba(10,10,12,0.8)' }} />

      {/* Modal card */}
      <div style={{
        position: 'absolute', left: 12, right: 12, top: 60, bottom: 60,
        background: theme.surface,
        borderRadius: isLib ? 4 : 16,
        boxShadow: theme.shadow,
        display: 'flex', flexDirection: 'column',
        border: `1px solid ${theme.border}`
      }}>
        <div style={{
          padding: '18px 20px 12px',
          fontFamily: isLib ? theme.serif : theme.sans,
          fontSize: isLib ? 26 : 19, fontWeight: isLib ? 600 : 600,
          fontStyle: isLib ? 'italic' : 'normal',
          color: theme.ink, borderBottom: `1px solid ${theme.border}`
        }}>Select chapter</div>
        <div style={{ flex: 1, overflow: 'auto' }}>
          {chapters.map((c, i) =>
          <div key={i} style={{
            display: 'flex', alignItems: 'flex-start', gap: 12,
            padding: '12px 20px',
            background: c.active ? theme.accentSoft : 'transparent',
            borderBottom: `1px solid ${theme.border}`,
            color: c.active ? theme.accent : theme.ink
          }}>
              <div style={{ flex: 1, fontSize: 13.5, lineHeight: 1.35,
              fontFamily: isLib && c.active ? theme.serif : theme.sans,
              fontStyle: isLib && c.active ? 'italic' : 'normal',
              fontWeight: c.active ? 600 : 400 }}>
                <span style={{ fontFamily: theme.mono, fontSize: 12, marginRight: 4 }}>{c.n}.</span>
                {c.title}
              </div>
              <div style={{ fontFamily: theme.mono, fontSize: 12, color: c.active ? theme.accent : theme.inkDim }}>{c.len}</div>
            </div>
          )}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', padding: '12px 20px', borderTop: `1px solid ${theme.border}`, gap: 18 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: theme.ink }}>
            <span style={{
              width: 14, height: 14, borderRadius: 3, background: theme.accent,
              display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff'
            }}><Icon d="M4 10l4 4 8-9" size={12} sw={2.4} /></span>
            Length
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: theme.inkDim }}>
            <span style={{
              width: 14, height: 14, borderRadius: 3, border: `1.5px solid ${theme.borderStrong}`
            }} />
            Position
          </label>
          <div style={{ flex: 1 }} />
          <button style={{
            background: 'none', border: 'none', color: theme.accent, fontWeight: 700,
            fontSize: 12, letterSpacing: 1.2, cursor: 'pointer', fontFamily: theme.sans
          }}>CANCEL</button>
        </div>
      </div>
    </PhoneShell>);

}

// ─────────────────────────────────────────────────────────
// SCREEN 4 — Characters relationship graph
// ─────────────────────────────────────────────────────────
function ScreenCharacters({ theme }) {
  const isLib = theme.name === 'library';
  // Character nodes positioned in % within graph area
  const chars = [
  { id: 'edmond', name: 'Edmond Dantès', role: 'Protagonist', x: 50, y: 38, r: 42, primary: true },
  { id: 'mercedes', name: 'Mercédès', role: 'Beloved', x: 18, y: 18, r: 28 },
  { id: 'fernand', name: 'Fernand', role: 'Rival', x: 82, y: 18, r: 28 },
  { id: 'danglars', name: 'Danglars', role: 'Conspirator', x: 84, y: 60, r: 26 },
  { id: 'villefort', name: 'Villefort', role: 'Prosecutor', x: 50, y: 78, r: 28 },
  { id: 'faria', name: 'Abbé Faria', role: 'Mentor', x: 16, y: 62, r: 26 }];

  // edges
  const edges = [
  ['edmond', 'mercedes', 'loves'],
  ['edmond', 'fernand', 'rivals'],
  ['edmond', 'danglars', 'betrayed by'],
  ['edmond', 'villefort', 'condemned by'],
  ['edmond', 'faria', 'mentored by'],
  ['mercedes', 'fernand', 'marries']];

  const node = (id) => chars.find((c) => c.id === id);
  return (
    <PhoneShell theme={theme}>
      <AppBar theme={theme} leading="back" title="Characters" sub="The Count of Monte Cristo · 6 noted"
      right={<>
          <button style={ibtn(theme)}>{Icons.add}</button>
          <button style={ibtn(theme)}>{Icons.more}</button>
        </>} />
      <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
        {/* graph container */}
        <div style={{ position: 'absolute', inset: '12px 14px 220px', borderRadius: isLib ? 4 : 14,
          background: isLib ? theme.surfaceAlt : theme.surface,
          border: `1px solid ${theme.border}`, overflow: 'hidden' }}>
          {/* subtle radial */}
          <div style={{ position: 'absolute', inset: 0,
            background: isLib ?
            'radial-gradient(circle at 50% 50%, rgba(180,69,30,0.06) 0%, transparent 60%)' :
            'radial-gradient(circle at 50% 50%, rgba(245,184,110,0.10) 0%, transparent 60%)'
          }} />
          {/* edges */}
          <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }} viewBox="0 0 100 100" preserveAspectRatio="none">
            {edges.map(([a, b, label], i) => {
              const A = node(a),B = node(b);
              return (
                <g key={i}>
                  <line x1={A.x} y1={A.y} x2={B.x} y2={B.y}
                  stroke={theme.borderStrong} strokeWidth="0.3"
                  strokeDasharray={label === 'loves' ? '0' : '1 1'}
                  opacity="0.7" />
                </g>);

            })}
          </svg>
          {/* nodes */}
          {chars.map((c) =>
          <div key={c.id} style={{
            position: 'absolute', left: `${c.x}%`, top: `${c.y}%`,
            transform: 'translate(-50%,-50%)',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4
          }}>
              <div style={{ ...{
                width: c.r, height: c.r, borderRadius: '50%',
                background: c.primary ? theme.accent : isLib ? theme.surfaceDeep : theme.surfaceAlt,
                color: c.primary ? isLib ? '#fff' : '#1a1208' : theme.ink,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: isLib ? theme.serif : theme.sans,
                fontSize: c.primary ? 16 : 13, fontWeight: 600,
                fontStyle: isLib ? 'italic' : 'normal',
                boxShadow: c.primary ? `0 0 0 4px ${theme.accentSoft}` : 'none',
                border: c.primary ? 'none' : `1px solid ${theme.borderStrong}`
              }, background: "rgb(55, 128, 99)" }}>{c.name.split(' ').map((w) => w[0]).join('').slice(0, 2)}</div>
              <div style={{ fontSize: 9.5, fontWeight: 600, color: theme.ink, textAlign: 'center', maxWidth: 64, lineHeight: 1.1 }}>{c.name}</div>
            </div>
          )}
        </div>

        {/* Selected character card */}
        <div style={{ position: 'absolute', left: 14, right: 14, bottom: 14,
          background: theme.surface, borderRadius: isLib ? 4 : 14,
          border: `1px solid ${theme.border}`, padding: 14,
          boxShadow: theme.shadow
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
            <div style={{
              width: 36, height: 36, borderRadius: '50%', background: theme.accent, color: '#fff',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 13
            }}>ED</div>
            <div style={{ flex: 1 }}>
              <div style={{
                fontFamily: isLib ? theme.serif : theme.sans,
                fontSize: isLib ? 18 : 15, fontWeight: 600, color: theme.ink,
                fontStyle: isLib ? 'italic' : 'normal'
              }}>Edmond Dantès</div>
              <div style={{ fontSize: 11, color: theme.inkDim, fontFamily: theme.mono }}>First noted · Part 1 · 00:12:04</div>
            </div>
            <button style={ibtn(theme)}>{Icons.bookmark}</button>
          </div>
          <div style={{ fontSize: 12.5, lineHeight: 1.45, color: theme.inkDim,
            fontFamily: isLib ? theme.serif : theme.sans,
            fontStyle: isLib ? 'italic' : 'normal'
          }}>
            Young Marseille sailor, falsely imprisoned. Watch for the “Pharaon” and his fiancée at the wharf — he doesn't know about Danglars's letter yet.
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 10, flexWrap: 'wrap' }}>
            {['protagonist', 'sailor', 'imprisoned'].map((t) =>
            <span key={t} style={{
              fontSize: 10, letterSpacing: 0.5, padding: '3px 8px', borderRadius: 999,
              background: theme.accentSoft, color: theme.accent, fontFamily: theme.mono
            }}>{t}</span>
            )}
          </div>
        </div>
      </div>
    </PhoneShell>);

}

// ─────────────────────────────────────────────────────────
// SCREEN 5 — Bookmarks with audio quotes
// ─────────────────────────────────────────────────────────
function ScreenBookmarks({ theme }) {
  const isLib = theme.name === 'library';
  const marks = [
  { time: '02:14:38', chapter: 'Part 3 · The Catalans', note: '“I should like to see the country where my Edmond was born.”',
    tag: 'quote', dur: '0:18', wave: [3, 5, 4, 7, 6, 9, 7, 8, 6, 7, 9, 11, 8, 7, 5, 7, 6, 5, 4, 3, 5, 4, 6, 7, 5, 4] },
  { time: '05:48:12', chapter: 'Part 6 · Château d\'If', note: 'First mention of the treasure map — Abbé Faria reveals the cipher.',
    tag: 'note', dur: null, wave: null },
  { time: '07:32:01', chapter: 'Part 8 · The Escape', note: '“Wait and hope.” — His last words to Edmond.',
    tag: 'quote', dur: '0:09', wave: [2, 3, 5, 6, 4, 7, 8, 9, 7, 6, 5, 7, 6, 4, 3, 2, 3, 5, 4, 3, 2] },
  { time: '11:03:47', chapter: 'Part 11 · The Smugglers', note: null,
    tag: 'pin', dur: null, wave: null }];

  return (
    <PhoneShell theme={theme}>
      <AppBar theme={theme} leading="back" title="Bookmarks" sub="The Count of Monte Cristo · 12 marks"
      right={<>
          <button style={ibtn(theme)}>{Icons.search}</button>
          <button style={ibtn(theme)}>{Icons.more}</button>
        </>} />
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 0 16px' }}>
        {marks.map((m, i) =>
        <div key={i} style={{
          margin: '6px 12px', padding: 14,
          background: theme.surface,
          border: `1px solid ${theme.border}`,
          borderRadius: isLib ? 4 : 12,
          borderLeft: `3px solid ${theme.accent}`
        }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: m.note ? 8 : 0 }}>
              <span style={{ fontFamily: theme.mono, fontSize: 13, color: theme.accent, fontWeight: 700 }}>{m.time}</span>
              <span style={{ fontSize: 11, color: theme.inkDim, flex: 1 }}>{m.chapter}</span>
              <span style={{
              fontSize: 9.5, letterSpacing: 0.8, padding: '2px 6px', borderRadius: 3,
              background: theme.accentSoft, color: theme.accent, fontFamily: theme.mono, textTransform: 'uppercase'
            }}>{m.tag}</span>
            </div>
            {m.note &&
          <div style={{
            fontSize: 13, lineHeight: 1.45, color: theme.ink,
            fontFamily: m.tag === 'quote' ? theme.serif : theme.sans,
            fontStyle: m.tag === 'quote' ? 'italic' : 'normal',
            marginBottom: m.wave ? 10 : 0
          }}>{m.note}</div>
          }
            {m.wave &&
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <button style={{
              width: 30, height: 30, borderRadius: '50%', background: theme.accent, color: '#fff',
              border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer'
            }}><Icon d="M7 4l13 8L7 20z" size={12} fill="currentColor" stroke="none" /></button>
                <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 1.5, height: 22 }}>
                  {m.wave.map((h, j) =>
              <div key={j} style={{
                flex: 1, height: `${h * 7}%`, minHeight: 2,
                background: j < m.wave.length * 0.3 ? theme.accent : theme.borderStrong,
                borderRadius: 1
              }} />
              )}
                </div>
                <span style={{ fontFamily: theme.mono, fontSize: 11, color: theme.inkDim }}>{m.dur}</span>
              </div>
          }
          </div>
        )}
      </div>
    </PhoneShell>);

}

// ─────────────────────────────────────────────────────────
// SCREEN 6 — Sleep timer with fade-out + rewind-on-wake
// ─────────────────────────────────────────────────────────
function ScreenSleep({ theme }) {
  const isLib = theme.name === 'library';
  return (
    <PhoneShell theme={theme}>
      {/* ambient bg */}
      {!isLib &&
      <div style={{ position: 'absolute', inset: 0, opacity: 0.3, filter: 'blur(40px)' }}>
          <Cover id="monte" />
        </div>
      }
      <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
        <AppBar theme={theme} leading="back" title="Sleep timer"
        right={<button style={{ ...ibtn(theme), color: theme.accent, fontWeight: 700, fontFamily: theme.mono, fontSize: 13 }}>OFF</button>} />

        {/* Big dial */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '28px 16px 12px' }}>
          <div style={{ position: 'relative', width: 240, height: 240 }}>
            {/* dial bg */}
            <svg viewBox="0 0 100 100" style={{ width: '100%', height: '100%' }}>
              <circle cx="50" cy="50" r="44" fill="none" stroke={theme.border} strokeWidth="1.5" />
              <circle cx="50" cy="50" r="44" fill="none" stroke={theme.accent} strokeWidth="2.5"
              strokeDasharray="276" strokeDashoffset="92"
              transform="rotate(-90 50 50)" strokeLinecap="round" />
              {/* tick marks */}
              {Array.from({ length: 60 }).map((_, i) => {
                const big = i % 5 === 0;
                const a = i / 60 * Math.PI * 2 - Math.PI / 2;
                const r1 = big ? 48 : 49;
                const r2 = big ? 51 : 50;
                return (
                  <line key={i}
                  x1={50 + Math.cos(a) * r1} y1={50 + Math.sin(a) * r1}
                  x2={50 + Math.cos(a) * r2} y2={50 + Math.sin(a) * r2}
                  stroke={theme.borderStrong} strokeWidth={big ? 0.8 : 0.4} />);

              })}
            </svg>
            <div style={{
              position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
              alignItems: 'center', justifyContent: 'center', gap: 4
            }}>
              <div style={{
                fontFamily: theme.mono, fontSize: 56, fontWeight: 300,
                color: theme.ink, lineHeight: 1, letterSpacing: -2
              }}>23:14</div>
              <div style={{ fontSize: 11, color: theme.inkDim, letterSpacing: 1.4, textTransform: 'uppercase' }}>remaining</div>
              <div style={{ marginTop: 6, fontSize: 11, color: theme.accent, fontFamily: theme.mono }}>fade begins · 22:14</div>
            </div>
          </div>
          {/* presets */}
          <div style={{ display: 'flex', gap: 8, marginTop: 18, flexWrap: 'wrap', justifyContent: 'center' }}>
            {['5 m', '15 m', '30 m', '45 m', '1 h', 'End of chapter'].map((p, i) =>
            <div key={p} style={{
              padding: '7px 12px', borderRadius: 999,
              background: i === 2 ? theme.accent : isLib ? theme.surface : theme.surfaceAlt,
              color: i === 2 ? '#fff' : theme.ink,
              border: `1px solid ${i === 2 ? theme.accent : theme.border}`,
              fontSize: 12, fontFamily: theme.mono, fontWeight: 500
            }}>{p}</div>
            )}
          </div>
        </div>

        {/* Options list */}
        <div style={{ flex: 1, padding: '12px 16px', display: 'flex', flexDirection: 'column', gap: 0 }}>
          <SleepOption theme={theme} title="Fade out over" desc="Volume eases to silence over the last 60s" value="60 s" />
          <SleepOption theme={theme} title="Rewind on wake" desc="When you press play after a sleep, rewind by 30s so you don't miss anything" value="30 s" toggle />
          <SleepOption theme={theme} title="Shake to extend" desc="Shake the device to add 5 minutes during fade" value="On" toggle on />
          <SleepOption theme={theme} title="End at chapter break" desc="Wait until the current chapter ends, then sleep" value="Off" toggle />
        </div>
      </div>
    </PhoneShell>);

}

function SleepOption({ theme, title, desc, value, toggle, on }) {
  const isLib = theme.name === 'library';
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12, padding: '12px 0',
      borderTop: `1px solid ${theme.border}`
    }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 14, color: theme.ink, fontWeight: 500 }}>{title}</div>
        <div style={{ fontSize: 11.5, color: theme.inkDim, marginTop: 2, lineHeight: 1.4 }}>{desc}</div>
      </div>
      {toggle ?
      <div style={{
        width: 36, height: 20, borderRadius: 20,
        background: on ? theme.accent : theme.borderStrong,
        position: 'relative', flexShrink: 0
      }}>
          <div style={{
          position: 'absolute', top: 2, left: on ? 18 : 2,
          width: 16, height: 16, borderRadius: '50%', background: '#fff',
          boxShadow: '0 1px 3px rgba(0,0,0,0.2)'
        }} />
        </div> :

      <div style={{ fontFamily: theme.mono, fontSize: 13, color: theme.accent, fontWeight: 600 }}>{value}</div>
      }
    </div>);

}

// ─────────────────────────────────────────────────────────
// SCREEN 7 — Stats / streaks
// ─────────────────────────────────────────────────────────
function ScreenStats({ theme }) {
  const isLib = theme.name === 'library';
  // 6 weeks heatmap
  const heatmap = [
  [0, 1, 0, 2, 3, 2, 4],
  [1, 2, 1, 3, 2, 4, 3],
  [0, 0, 2, 2, 1, 3, 2],
  [2, 3, 1, 2, 4, 4, 3],
  [1, 2, 2, 3, 3, 2, 4],
  [3, 4, 2, 3, 4, 3, 0]];

  return (
    <PhoneShell theme={theme}>
      <AppBar theme={theme} leading="back" title="Your listening" right={
      <button style={ibtn(theme)}>{Icons.more}</button>
      } />
      <div style={{ flex: 1, overflow: 'auto', padding: '14px 14px 24px' }}>

        {/* Hero numbers */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 14 }}>
          <StatCard theme={theme} label="This week" big="6h 24m" accent="+1h 12m">
            <Sparkline theme={theme} data={[12, 38, 22, 55, 40, 68, 42]} />
          </StatCard>
          <StatCard theme={theme} label="Streak" big="14" subtitle="days" icon={Icons.flame} />
        </div>

        {/* Heatmap */}
        <div style={{
          background: theme.surface, border: `1px solid ${theme.border}`,
          borderRadius: isLib ? 4 : 12, padding: 14, marginBottom: 14
        }}>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10 }}>
            <div style={{
              fontFamily: isLib ? theme.serif : theme.sans,
              fontSize: isLib ? 17 : 14, fontWeight: 600, fontStyle: isLib ? 'italic' : 'normal',
              color: theme.ink
            }}>Last 6 weeks</div>
            <div style={{ fontSize: 11, color: theme.inkDim, fontFamily: theme.mono }}>32h 18m</div>
          </div>
          <div style={{ display: 'flex', gap: 4 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4, justifyContent: 'space-around', paddingRight: 4 }}>
              {['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((d, i) =>
              <div key={i} style={{ fontSize: 9, color: theme.inkLight, fontFamily: theme.mono, height: 14, display: 'flex', alignItems: 'center' }}>{d}</div>
              )}
            </div>
            {heatmap.map((week, wi) =>
            <div key={wi} style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 4 }}>
                {week.map((v, di) =>
              <div key={di} style={{
                height: 14, borderRadius: 2,
                background: v === 0 ? theme.border :
                v === 1 ? `rgba(${theme.name === 'library' ? '180,69,30' : '245,184,110'},0.25)` :
                v === 2 ? `rgba(${theme.name === 'library' ? '180,69,30' : '245,184,110'},0.5)` :
                v === 3 ? `rgba(${theme.name === 'library' ? '180,69,30' : '245,184,110'},0.75)` :
                theme.accent
              }} />
              )}
              </div>
            )}
          </div>
        </div>

        {/* Current book progress */}
        <div style={{
          background: theme.surface, border: `1px solid ${theme.border}`,
          borderRadius: isLib ? 4 : 12, padding: 14, marginBottom: 14,
          display: 'flex', gap: 14, alignItems: 'center'
        }}>
          <div style={{ width: 54, height: 74, borderRadius: isLib ? 2 : 4, overflow: 'hidden', flexShrink: 0,
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)' }}>
            <Cover id="monte" small />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{
              fontFamily: isLib ? theme.serif : theme.sans,
              fontSize: isLib ? 17 : 14, fontWeight: 600,
              fontStyle: isLib ? 'italic' : 'normal',
              color: theme.ink, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'
            }}>The Count of Monte Cristo</div>
            <div style={{ fontSize: 11, color: theme.inkDim, marginTop: 2, fontFamily: theme.mono }}>at this pace · finish Aug 14</div>
            <div style={{ height: 4, background: theme.border, borderRadius: 2, marginTop: 8 }}>
              <div style={{ width: '24%', height: '100%', background: theme.accent, borderRadius: 2 }} />
            </div>
          </div>
        </div>

        {/* Finished list */}
        <div style={{ fontSize: 11, letterSpacing: 1.2, color: theme.inkDim, fontWeight: 600, marginBottom: 8 }}>FINISHED THIS YEAR · 7</div>
        <div style={{ display: 'flex', gap: 8, overflowX: 'auto' }}>
          {['hobbit', 'fellowship', 'vicarage', 'paddington', 'threemen'].map((b, i) =>
          <div key={i} style={{ width: 64, flexShrink: 0 }}>
              <div style={{ width: '100%', aspectRatio: '3/4.4', borderRadius: isLib ? 2 : 4, overflow: 'hidden',
              boxShadow: '0 2px 6px rgba(0,0,0,0.15)', position: 'relative' }}>
                <Cover id={b} small />
                <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.3)' }} />
                <div style={{ position: 'absolute', top: 4, right: 4, color: '#fff' }}>
                  <Icon d="M5 12l5 5 9-11" size={14} sw={2.5} />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </PhoneShell>);

}

function StatCard({ theme, label, big, accent, subtitle, icon, children }) {
  const isLib = theme.name === 'library';
  return (
    <div style={{
      background: theme.surface, border: `1px solid ${theme.border}`,
      borderRadius: isLib ? 4 : 12, padding: 14
    }}>
      <div style={{ fontSize: 10.5, letterSpacing: 1, color: theme.inkDim, fontWeight: 600, textTransform: 'uppercase' }}>{label}</div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginTop: 4 }}>
        {icon && <span style={{ color: theme.accent }}>{icon}</span>}
        <span style={{
          fontFamily: isLib ? theme.serif : theme.mono,
          fontSize: isLib ? 30 : 28, fontWeight: isLib ? 600 : 500,
          color: theme.ink, lineHeight: 1,
          fontStyle: isLib ? 'italic' : 'normal'
        }}>{big}</span>
        {subtitle && <span style={{ fontSize: 12, color: theme.inkDim }}>{subtitle}</span>}
      </div>
      {accent && <div style={{ fontSize: 10.5, color: theme.accent, marginTop: 4, fontFamily: theme.mono }}>{accent}</div>}
      {children && <div style={{ marginTop: 10 }}>{children}</div>}
    </div>);

}

function Sparkline({ theme, data }) {
  const max = Math.max(...data);
  return (
    <div style={{ display: 'flex', gap: 3, alignItems: 'flex-end', height: 30 }}>
      {data.map((v, i) =>
      <div key={i} style={{
        flex: 1, height: `${v / max * 100}%`, minHeight: 2,
        background: theme.accent, opacity: 0.6 + v / max * 0.4,
        borderRadius: 1
      }} />
      )}
    </div>);

}

Object.assign(window, {
  THEMES, LIBRARY_THEME, STAGE_THEME,
  ScreenLibrary, ScreenPlayer, ScreenChapters,
  ScreenCharacters, ScreenBookmarks, ScreenSleep, ScreenStats
});
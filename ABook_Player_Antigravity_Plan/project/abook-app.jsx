// abook-app.jsx — Composes both directions in a DesignCanvas + Tweaks.

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "libraryAccent": "#b4451e",
  "stageAccent": "#f5b86e",
  "density": "standard",
  "showDirections": "both",
  "playerCover": "monte"
}/*EDITMODE-END*/;

const DENSITY_SCALE = { cozy: 0.92, standard: 1.0, spacious: 1.08 };

// Density CSS — scales text size in screens
function applyDensity(d) {
  const id = 'abook-density';
  let el = document.getElementById(id);
  if (!el) { el = document.createElement('style'); el.id = id; document.head.appendChild(el); }
  const s = DENSITY_SCALE[d] || 1;
  el.textContent = `.abook-screen{font-size:${14*s}px}`;
}

function PhoneArtboard({ theme, children, bookId }) {
  // AndroidDevice wraps the content. We don't pass title (no built-in app bar),
  // and toggle dark for status/nav bar colours.
  return (
    <AndroidDevice width={412} height={892} dark={theme.name === 'stage'}>
      <div className="abook-screen" style={{
        width:'100%', height:'100%', position:'relative', overflow:'hidden',
        background: theme.bg,
      }}>
        {children}
      </div>
    </AndroidDevice>
  );
}

function makeTheme(base, accent) {
  // Replace accent and accentSoft (computed from accent with alpha)
  // accentSoft: 14% alpha
  const hex = accent.replace('#','');
  const r = parseInt(hex.slice(0,2),16);
  const g = parseInt(hex.slice(2,4),16);
  const b = parseInt(hex.slice(4,6),16);
  return {
    ...base,
    accent,
    accentSoft: `rgba(${r},${g},${b},0.14)`,
  };
}

const SCREENS = [
  { id: 'library',     label: 'Library',     Component: ScreenLibrary },
  { id: 'player',      label: 'Now Playing', Component: ScreenPlayer },
  { id: 'chapters',    label: 'Chapters',    Component: ScreenChapters },
  { id: 'characters',  label: 'Characters',  Component: ScreenCharacters },
  { id: 'bookmarks',   label: 'Bookmarks',   Component: ScreenBookmarks },
  { id: 'sleep',       label: 'Sleep timer', Component: ScreenSleep },
  { id: 'stats',       label: 'Listening',   Component: ScreenStats },
];

function App() {
  const [t, setT] = useTweaks(TWEAK_DEFAULTS);

  React.useEffect(() => { applyDensity(t.density); }, [t.density]);

  const libTheme   = makeTheme(LIBRARY_THEME, t.libraryAccent);
  const stageTheme = makeTheme(STAGE_THEME, t.stageAccent);

  const dirs = [];
  if (t.showDirections === 'both' || t.showDirections === 'library')
    dirs.push({ id: 'A', label: 'A · Library', subtitle: 'Warm, literary, paper. Type-led.', theme: libTheme });
  if (t.showDirections === 'both' || t.showDirections === 'stage')
    dirs.push({ id: 'B', label: 'B · Stage',   subtitle: 'Dark, cinematic, cover-art-first.', theme: stageTheme });

  return (
    <>
      <DesignCanvas>
        {dirs.map(dir => (
          <DCSection key={dir.id} id={dir.id} title={dir.label} subtitle={dir.subtitle}>
            {SCREENS.map(s => (
              <DCArtboard key={s.id} id={`${dir.id}-${s.id}`} label={s.label} width={428} height={908}
                style={{ background:'transparent', boxShadow:'none' }}>
                <PhoneArtboard theme={dir.theme} bookId={t.playerCover}>
                  <s.Component theme={dir.theme} bookId={t.playerCover}/>
                </PhoneArtboard>
              </DCArtboard>
            ))}
          </DCSection>
        ))}
      </DesignCanvas>

      <TweaksPanel title="Tweaks">
        <TweakSection label="Aesthetic" />
        <TweakRadio label="Show" value={t.showDirections}
          options={[
            { value:'both',    label:'Both' },
            { value:'library', label:'Library' },
            { value:'stage',   label:'Stage' },
          ]}
          onChange={v => setT('showDirections', v)}/>

        <TweakSection label="Library accent (A)" />
        <TweakColor label="Color"
          value={t.libraryAccent}
          options={['#b4451e', '#1f7a72', '#7a3461', '#2a3a4a']}
          onChange={v => setT('libraryAccent', v)}/>

        <TweakSection label="Stage accent (B)" />
        <TweakColor label="Color"
          value={t.stageAccent}
          options={['#f5b86e', '#6fa8ff', '#5fd0a4', '#c894ff']}
          onChange={v => setT('stageAccent', v)}/>

        <TweakSection label="Density" />
        <TweakRadio label="Text" value={t.density}
          options={[
            { value:'cozy',      label:'Cozy' },
            { value:'standard',  label:'Std' },
            { value:'spacious',  label:'Spacious' },
          ]}
          onChange={v => setT('density', v)}/>

        <TweakSection label="Player book" />
        <TweakSelect label="Cover" value={t.playerCover}
          options={[
            { value:'monte',      label:'The Count of Monte Cristo' },
            { value:'hound',      label:'The Hound of the Baskervilles' },
            { value:'hobbit',     label:'The Hobbit' },
            { value:'fellowship', label:'The Fellowship of the Ring' },
            { value:'threemen',   label:'Three Men in a Boat' },
            { value:'vicarage',   label:'The Murder at the Vicarage' },
            { value:'paddington', label:'4.50 from Paddington' },
          ]}
          onChange={v => setT('playerCover', v)}/>
      </TweaksPanel>
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App/>);

// abook-covers.jsx — CSS-generated stylized book covers
// Each cover is a small composition: gradient/texture + typographic title.
// No external images. Themed accents are independent (covers stay vibrant).

const COVER_SERIF = '"Cormorant Garamond", "EB Garamond", "Playfair Display", Georgia, serif';
const COVER_SANS = '"Inter", "Helvetica Neue", Arial, sans-serif';

// Helper: noise/grain overlay as data-uri svg (works on dark and light)
const grainUrl = (opacity = 0.18) =>
  `url("data:image/svg+xml,%3Csvg viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='1.6' numOctaves='2' stitchTiles='stitch'/%3E%3CfeColorMatrix values='0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 ${opacity} 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E")`;

// ─────────────────────────────────────────────────────────
// Individual cover components — each ~3:4.5 portrait
// Sized by parent (width:100%, aspect-ratio enforced by container)
// ─────────────────────────────────────────────────────────

function CoverHound({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'radial-gradient(120% 80% at 50% 110%, #0c1a26 0%, #050a12 40%, #1a2638 100%)',
      overflow: 'hidden', color: '#dfe4ed', fontFamily: COVER_SERIF,
    }}>
      {/* moon */}
      <div style={{ position:'absolute', top:'14%', right:'18%', width:'34%', aspectRatio:'1', borderRadius:'50%',
        background:'radial-gradient(circle at 30% 30%, #f1e6c4 0%, #d6c490 55%, transparent 70%)',
        filter:'blur(0.6px)', opacity:0.85 }} />
      {/* fog bands */}
      <div style={{ position:'absolute', inset:0, background:
        'linear-gradient(transparent 40%, rgba(180,200,220,0.08) 55%, transparent 70%), linear-gradient(transparent 60%, rgba(180,200,220,0.06) 75%, transparent 90%)' }} />
      {/* hound silhouette (CSS-only) */}
      <div style={{ position:'absolute', bottom:'8%', left:'50%', transform:'translateX(-50%)', width:'62%', height:'40%' }}>
        <svg viewBox="0 0 100 70" style={{ width:'100%', height:'100%', display:'block' }}>
          <path d="M5 65 L10 50 L8 35 Q15 28 22 30 L26 22 Q30 16 36 20 L40 14 Q44 8 50 16 L56 22 Q64 18 70 26 L78 32 Q86 36 88 50 L92 65 Z" fill="#020509"/>
          <circle cx="40" cy="22" r="1.5" fill="#f1c453"/>
          <circle cx="48" cy="20" r="1.5" fill="#f1c453"/>
        </svg>
      </div>
      <div style={{ position:'absolute', inset:0, backgroundImage: grainUrl(0.12), mixBlendMode:'overlay' }} />
      <div style={{ position:'absolute', top:'5%', left:0, right:0, textAlign:'center', color:'#cbd2db', fontSize: small?7:10, letterSpacing:2, textTransform:'uppercase', fontFamily: COVER_SANS }}>
        Arthur Conan Doyle
      </div>
      <div style={{ position:'absolute', bottom:'2%', left:0, right:0, textAlign:'center', color:'#f1c453',
        fontSize: small?14:22, lineHeight:1, fontStyle:'italic', textShadow:'0 2px 8px rgba(0,0,0,0.6)' }}>
        Hound<br/>
        <span style={{ fontSize: small?7:10, letterSpacing:3, color:'#cbd2db', fontStyle:'normal' }}>of the baskervilles</span>
      </div>
    </div>
  );
}

function CoverMonteCristo({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'linear-gradient(170deg, #f5b46b 0%, #d97843 35%, #6e3a2a 70%, #1d1410 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#f7e6cf',
    }}>
      {/* sun */}
      <div style={{ position:'absolute', top:'30%', left:'18%', width:'30%', aspectRatio:'1', borderRadius:'50%',
        background:'radial-gradient(circle, #fff7d8 0%, #ffc77a 55%, transparent 75%)' }} />
      {/* sea */}
      <div style={{ position:'absolute', bottom:0, left:0, right:0, height:'42%',
        background:'linear-gradient(180deg, #2c4a5c 0%, #0d1620 100%)' }} />
      {/* fortress silhouette */}
      <div style={{ position:'absolute', bottom:'38%', right:'12%', width:'42%', height:'30%' }}>
        <svg viewBox="0 0 100 60" style={{ width:'100%', height:'100%' }}>
          <path d="M0 60 L0 35 L15 30 L25 18 L35 14 L40 10 L48 6 L55 10 L62 14 L70 18 L80 26 L92 30 L100 36 L100 60 Z" fill="#1a0e0a"/>
          <rect x="40" y="14" width="4" height="6" fill="#0a0606"/>
          <rect x="50" y="14" width="4" height="8" fill="#0a0606"/>
        </svg>
      </div>
      <div style={{ position:'absolute', inset:0, backgroundImage: grainUrl(0.14), mixBlendMode:'multiply' }} />
      <div style={{ position:'absolute', top:'6%', left:0, right:0, textAlign:'center',
        fontSize: small?7:10, letterSpacing:3, fontFamily: COVER_SANS, color:'#fff' }}>
        ALEXANDRE DUMAS
      </div>
      <div style={{ position:'absolute', bottom:'4%', left:0, right:0, textAlign:'center',
        fontSize: small?13:20, lineHeight:1, color:'#fff', textShadow:'0 2px 10px rgba(0,0,0,0.5)' }}>
        The Count of<br/>
        <span style={{ fontStyle:'italic', fontSize: small?15:24 }}>Monte Cristo</span>
      </div>
    </div>
  );
}

function CoverHobbit({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'radial-gradient(110% 80% at 50% 60%, #f08a2e 0%, #b04017 30%, #3a1108 70%, #0a0303 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#f7d27a',
    }}>
      {/* dark branches */}
      <svg viewBox="0 0 100 150" preserveAspectRatio="none" style={{ position:'absolute', inset:0, width:'100%', height:'100%' }}>
        <path d="M0 0 L20 30 L10 60 L25 90 L5 130 L0 150 Z" fill="#0a0303" opacity="0.9"/>
        <path d="M100 0 L80 30 L92 60 L75 90 L95 130 L100 150 Z" fill="#0a0303" opacity="0.9"/>
        <path d="M0 80 L25 75 L15 95 L0 100 Z" fill="#1a0606"/>
        <path d="M100 80 L75 75 L85 95 L100 100 Z" fill="#1a0606"/>
      </svg>
      <div style={{ position:'absolute', inset:0, backgroundImage: grainUrl(0.18), mixBlendMode:'overlay' }} />
      <div style={{ position:'absolute', top:'10%', left:0, right:0, textAlign:'center',
        fontSize: small?7:10, letterSpacing:3, color:'#f7d27a', fontFamily: COVER_SANS }}>
        J. R. R. TOLKIEN
      </div>
      <div style={{ position:'absolute', bottom:'8%', left:0, right:0, textAlign:'center',
        fontSize: small?22:36, lineHeight:0.9, color:'#f7d27a', fontWeight:600, letterSpacing:1, textShadow:'0 2px 8px rgba(0,0,0,0.6)' }}>
        THE<br/>HOBBIT
      </div>
    </div>
  );
}

function CoverFellowship({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'linear-gradient(180deg, #1c2820 0%, #0c1410 60%, #060906 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#c8c2a8',
    }}>
      {/* rider silhouette */}
      <svg viewBox="0 0 100 120" preserveAspectRatio="xMidYMid slice" style={{ position:'absolute', inset:0, width:'100%', height:'100%' }}>
        <path d="M0 80 Q20 76 35 78 L40 70 L48 65 L52 58 L58 62 L64 70 L72 78 Q86 76 100 80 L100 120 L0 120 Z" fill="#02040a"/>
        <path d="M40 70 L42 50 L50 45 L56 42 L62 48 L60 58 Z" fill="#02040a"/>
      </svg>
      <div style={{ position:'absolute', inset:0, backgroundImage: grainUrl(0.18), mixBlendMode:'overlay' }} />
      <div style={{ position:'absolute', top:'7%', left:0, right:0, textAlign:'center',
        fontSize: small?6:9, letterSpacing:3, color:'#c8c2a8', fontFamily: COVER_SANS }}>
        J. R. R. TOLKIEN
      </div>
      <div style={{ position:'absolute', bottom:'8%', left:0, right:0, textAlign:'center',
        fontSize: small?10:14, lineHeight:1, color:'#e6d8a8', fontStyle:'italic',
        fontFamily:COVER_SERIF, textShadow:'0 2px 8px rgba(0,0,0,0.6)' }}>
        The Fellowship<br/>of the Ring
      </div>
    </div>
  );
}

function CoverThreeMen({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'linear-gradient(180deg, #f4f0e6 0%, #e6dccb 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#1a1a1a',
    }}>
      <div style={{ position:'absolute', top:'8%', left:0, right:0, textAlign:'center',
        fontSize: small?7:9, letterSpacing:2, fontFamily: COVER_SANS, fontWeight:700 }}>
        JEROME K JEROME
      </div>
      <div style={{ position:'absolute', top:'22%', left:0, right:0, textAlign:'center',
        lineHeight:0.95, fontWeight:700 }}>
        <div style={{ fontSize: small?20:32 }}>THREE</div>
        <div style={{ fontSize: small?20:32, color:'#d9531e' }}>MEN IN</div>
        <div style={{ fontSize: small?20:32 }}>A BOAT</div>
      </div>
      {/* boat shape */}
      <svg viewBox="0 0 100 40" style={{ position:'absolute', bottom:'8%', left:0, width:'100%' }}>
        <path d="M10 20 Q50 30 90 20 L80 30 Q50 38 20 30 Z" fill="#13243a"/>
        <circle cx="35" cy="16" r="4" fill="#13243a"/>
        <circle cx="50" cy="14" r="4" fill="#13243a"/>
        <circle cx="65" cy="16" r="4" fill="#13243a"/>
        <path d="M0 32 Q50 36 100 32 L100 40 L0 40 Z" fill="#b8d8e0"/>
      </svg>
      <div style={{ position:'absolute', bottom:'2%', left:0, right:0, textAlign:'center',
        fontSize: small?6:8, color:'#d9531e', letterSpacing:1, fontStyle:'italic' }}>
        ‘A COMIC MASTERPIECE’
      </div>
    </div>
  );
}

function CoverVicarage({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'linear-gradient(180deg, #2c4a5c 0%, #4a6a7e 50%, #e8e6e0 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#1d2c3a',
    }}>
      <div style={{ position:'absolute', top:'4%', left:'8%', background:'#d83128', color:'#fff',
        padding: small?'2px 4px':'3px 6px', fontSize: small?5:7, fontWeight:700, letterSpacing:1, fontFamily: COVER_SANS }}>
        MISS MARPLE
      </div>
      <div style={{ position:'absolute', top:'10%', left:0, right:0, textAlign:'center',
        fontSize: small?5:7, letterSpacing:2, fontFamily: COVER_SANS, color:'#1d2c3a' }}>
        THE QUEEN OF MYSTERY
      </div>
      <div style={{ position:'absolute', top:'17%', left:0, right:0, textAlign:'center',
        fontSize: small?16:26, color:'#fff', fontStyle:'italic', textShadow:'0 1px 4px rgba(0,0,0,0.3)',
        fontFamily:'"Brush Script MT", cursive' }}>
        Agatha Christie
      </div>
      {/* gravestone */}
      <div style={{ position:'absolute', bottom:'4%', left:'25%', right:'25%', height:'52%',
        background: 'linear-gradient(180deg, #c9c1a6 0%, #a89c7a 100%)',
        borderRadius:'50% 50% 4px 4px / 30% 30% 4px 4px',
        boxShadow:'0 4px 12px rgba(0,0,0,0.3)' }} />
      <div style={{ position:'absolute', bottom:'28%', left:0, right:0, textAlign:'center', zIndex:1,
        color:'#1d2c3a', fontSize: small?9:14, lineHeight:1, fontWeight:700 }}>
        THE<br/>MURDER<br/>AT THE<br/>VICARAGE
      </div>
    </div>
  );
}

function CoverPaddington({ small }) {
  return (
    <div style={{
      position: 'relative', width: '100%', height: '100%',
      background: 'linear-gradient(180deg, #e8843a 0%, #c44d1e 100%)',
      overflow: 'hidden', fontFamily: COVER_SERIF, color: '#fff',
    }}>
      {/* railway converging lines */}
      <svg viewBox="0 0 100 150" preserveAspectRatio="none" style={{ position:'absolute', inset:0, width:'100%', height:'100%' }}>
        <path d="M40 60 L0 150 L30 150 L48 60 Z" fill="#1a0a08" opacity="0.85"/>
        <path d="M60 60 L100 150 L70 150 L52 60 Z" fill="#1a0a08" opacity="0.85"/>
        <line x1="30" y1="100" x2="70" y2="100" stroke="#1a0a08" strokeWidth="1.5"/>
        <line x1="20" y1="120" x2="80" y2="120" stroke="#1a0a08" strokeWidth="1.5"/>
      </svg>
      <div style={{ position:'absolute', top:'12%', left:0, right:0, textAlign:'center',
        fontSize: small?14:22, color:'#fff', fontFamily:'"Brush Script MT", cursive', fontStyle:'italic',
        textShadow:'0 1px 4px rgba(0,0,0,0.2)' }}>
        Agatha<br/>Christie
      </div>
      <div style={{ position:'absolute', bottom:'4%', left:0, right:0, textAlign:'center',
        fontSize: small?13:20, fontWeight:700, color:'#fff' }}>
        4.50 FROM<br/>PADDINGTON
      </div>
    </div>
  );
}

// Registry
const COVERS = {
  hound: CoverHound,
  monte: CoverMonteCristo,
  hobbit: CoverHobbit,
  fellowship: CoverFellowship,
  threemen: CoverThreeMen,
  vicarage: CoverVicarage,
  paddington: CoverPaddington,
};

function Cover({ id, small, style }) {
  const C = COVERS[id] || COVERS.hound;
  return <div style={{ width:'100%', height:'100%', ...style }}><C small={small}/></div>;
}

Object.assign(window, { Cover, COVERS });

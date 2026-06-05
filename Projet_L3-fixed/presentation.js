const pptxgen = require("pptxgenjs");

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.title = "Bomberman — Présentation Projet";

// ── Palette ──────────────────────────────────────────────────────────────────
const C = {
  dark:    "0D0D1A",   // fond sombre titre
  navy:    "12122A",   // fond intermédiaire
  card:    "1A1A35",   // fond carte
  accent:  "F5C518",   // jaune bombe
  blue:    "3B8CE8",   // joueur 1
  red:     "E84040",   // joueur 2
  green:   "44CC88",   // succès / bot
  white:   "FFFFFF",
  light:   "E8E8F0",
  muted:   "8888AA",
  border:  "2A2A50",
};

const makeShadow = () => ({ type: "outer", blur: 8, offset: 3, angle: 135, color: "000000", opacity: 0.35 });

// ── Helpers ───────────────────────────────────────────────────────────────────
function addSlideHeader(slide, label) {
  // petite pastille colorée + label section en haut à gauche
  slide.addShape(pres.shapes.RECTANGLE, {
    x: 0.4, y: 0.18, w: 0.06, h: 0.3,
    fill: { color: C.accent }, line: { color: C.accent }
  });
  slide.addText(label, {
    x: 0.55, y: 0.14, w: 4, h: 0.38,
    fontSize: 10, color: C.muted, bold: true, align: "left",
    fontFace: "Consolas", margin: 0
  });
}

function addTitle(slide, title, sub, y = 0.55) {
  slide.addText(title, {
    x: 0.4, y, w: 9.2, h: 0.75,
    fontSize: 32, bold: true, color: C.white, fontFace: "Calibri",
    align: "left", margin: 0
  });
  if (sub) {
    slide.addText(sub, {
      x: 0.4, y: y + 0.78, w: 9.2, h: 0.4,
      fontSize: 14, color: C.muted, fontFace: "Calibri", align: "left", margin: 0
    });
  }
}

function addCard(slide, x, y, w, h, opts = {}) {
  slide.addShape(pres.shapes.RECTANGLE, {
    x, y, w, h,
    fill: { color: opts.fill || C.card },
    line: { color: opts.border || C.border, pt: 1 },
    shadow: makeShadow()
  });
}

function addAccentBar(slide, x, y, h, color = C.accent) {
  slide.addShape(pres.shapes.RECTANGLE, {
    x, y, w: 0.06, h,
    fill: { color }, line: { color }
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 1 — Titre
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };

  // Gros bloc fond accent à gauche
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0, y: 0, w: 0.18, h: 5.625,
    fill: { color: C.accent }, line: { color: C.accent }
  });

  // Emoji bombe stylisé (texte)
  s.addText("💣", {
    x: 0.5, y: 0.7, w: 2, h: 2,
    fontSize: 96, align: "left", margin: 0
  });

  // Titre principal
  s.addText("BOMBERMAN", {
    x: 0.5, y: 2.4, w: 9, h: 1.1,
    fontSize: 64, bold: true, color: C.accent,
    fontFace: "Calibri", charSpacing: 8, margin: 0
  });

  s.addText("Projet Java — Architecture MVC", {
    x: 0.5, y: 3.45, w: 9, h: 0.5,
    fontSize: 20, color: C.light, fontFace: "Calibri", margin: 0
  });

  s.addText("Jeu 2D inspiré de l'arcade classique · Java Swing · IA · Mouvement pixel", {
    x: 0.5, y: 3.95, w: 9, h: 0.35,
    fontSize: 12, color: C.muted, fontFace: "Calibri", margin: 0
  });

  // Badges technos en bas
  const badges = ["Java 17", "Swing", "MVC", "BotAI", "Timer Loop"];
  badges.forEach((b, i) => {
    const bx = 0.5 + i * 1.82;
    s.addShape(pres.shapes.RECTANGLE, {
      x: bx, y: 4.9, w: 1.65, h: 0.42,
      fill: { color: C.card }, line: { color: C.border, pt: 1 }
    });
    s.addText(b, {
      x: bx, y: 4.9, w: 1.65, h: 0.42,
      fontSize: 11, bold: true, color: C.accent,
      fontFace: "Consolas", align: "center", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 2 — Sommaire
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "SOMMAIRE");
  addTitle(s, "Plan de la présentation", null, 0.55);

  const items = [
    { num: "01", label: "Présentation du projet",      color: C.accent },
    { num: "02", label: "Technologies utilisées",       color: C.blue   },
    { num: "03", label: "Architecture MVC",             color: C.green  },
    { num: "04", label: "Structure des packages",       color: C.accent },
    { num: "05", label: "Modèle — logique de jeu",      color: C.blue   },
    { num: "06", label: "Vue — rendu graphique",        color: C.red    },
    { num: "07", label: "Contrôleur — boucle de jeu",   color: C.green  },
    { num: "08", label: "Intelligence artificielle",    color: C.accent },
    { num: "09", label: "Mouvement pixel fluide",       color: C.blue   },
    { num: "10", label: "Système d'items & bombes",     color: C.red    },
    { num: "11", label: "Gestion des collisions",       color: C.green  },
    { num: "12", label: "Défis & solutions",            color: C.accent },
    { num: "13", label: "Conclusion & perspectives",    color: C.blue   },
  ];

  const col1 = items.slice(0, 7);
  const col2 = items.slice(7);

  col1.forEach((item, i) => {
    const y = 1.3 + i * 0.53;
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.4, y, w: 0.55, h: 0.38,
      fill: { color: item.color }, line: { color: item.color }
    });
    s.addText(item.num, {
      x: 0.4, y, w: 0.55, h: 0.38,
      fontSize: 12, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    s.addText(item.label, {
      x: 1.05, y: y + 0.02, w: 3.8, h: 0.35,
      fontSize: 13, color: C.light, fontFace: "Calibri", margin: 0
    });
  });

  col2.forEach((item, i) => {
    const y = 1.3 + i * 0.53;
    s.addShape(pres.shapes.RECTANGLE, {
      x: 5.2, y, w: 0.55, h: 0.38,
      fill: { color: item.color }, line: { color: item.color }
    });
    s.addText(item.num, {
      x: 5.2, y, w: 0.55, h: 0.38,
      fontSize: 12, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    s.addText(item.label, {
      x: 5.85, y: y + 0.02, w: 3.8, h: 0.35,
      fontSize: 13, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 3 — Présentation du projet
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "01 — PRÉSENTATION");
  addTitle(s, "Qu'est-ce que Bomberman ?", "Un jeu arcade 2D en Java recréé from scratch");

  // Carte gauche — description
  addCard(s, 0.4, 1.45, 5.6, 3.6);
  addAccentBar(s, 0.4, 1.45, 3.6);
  s.addText("Le projet", {
    x: 0.6, y: 1.55, w: 5.2, h: 0.4,
    fontSize: 16, bold: true, color: C.accent, fontFace: "Calibri", margin: 0
  });
  const desc = [
    { text: "Bomberman est un jeu vidéo d'arcade classique (1983). Ce projet en est une réimplémentation complète en Java.", options: { breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Le joueur pose des bombes pour détruire des blocs et éliminer l'adversaire dans un labyrinthe.", options: { breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Le projet a été développé en respectant le patron d'architecture MVC (Modèle-Vue-Contrôleur) pour garantir une séparation claire des responsabilités.", options: {} },
  ];
  s.addText(desc, {
    x: 0.6, y: 2.05, w: 5.2, h: 2.8,
    fontSize: 13, color: C.light, fontFace: "Calibri",
    valign: "top", margin: 0
  });

  // Carte droite — chiffres clés
  addCard(s, 6.2, 1.45, 3.4, 3.6);
  s.addText("Chiffres clés", {
    x: 6.4, y: 1.55, w: 3.0, h: 0.4,
    fontSize: 16, bold: true, color: C.green, fontFace: "Calibri", margin: 0
  });

  const stats = [
    { val: "13",  label: "classes Java" },
    { val: "3",   label: "packages MVC" },
    { val: "2",   label: "modes de jeu" },
    { val: "3",   label: "niveaux de difficulté IA" },
    { val: "4",   label: "types d'items" },
  ];
  stats.forEach((st, i) => {
    const y = 2.1 + i * 0.63;
    s.addText(st.val, {
      x: 6.4, y, w: 0.7, h: 0.5,
      fontSize: 26, bold: true, color: C.accent,
      fontFace: "Calibri", align: "center", margin: 0
    });
    s.addText(st.label, {
      x: 7.15, y: y + 0.08, w: 2.2, h: 0.38,
      fontSize: 12, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 4 — Technologies
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "02 — TECHNOLOGIES");
  addTitle(s, "Technologies utilisées", "Stack 100% Java standard, sans dépendance externe");

  const techs = [
    { name: "Java 17",        desc: "Langage principal. Records, switch expressions, streams.",              color: C.accent },
    { name: "Java Swing",     desc: "Framework UI : JFrame, JPanel, KeyAdapter, Timer Swing.",               color: C.blue   },
    { name: "java.awt",       desc: "Graphics2D, RenderingHints, AlphaComposite pour le rendu avancé.",      color: C.red    },
    { name: "javax.swing.Timer", desc: "Boucle de jeu à ~33 fps (30ms/tick) sur l'EDT Swing.",              color: C.green  },
    { name: "java.util",      desc: "ArrayList, HashMap, HashSet, LinkedList pour toutes les structures.",   color: C.accent },
    { name: "MVC Pattern",    desc: "Architecture logicielle : Model / View / Controller séparés.",          color: C.blue   },
  ];

  techs.forEach((t, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.4 + col * 4.85;
    const y = 1.45 + row * 1.38;
    addCard(s, x, y, 4.55, 1.2);
    addAccentBar(s, x, y, 1.2, t.color);
    s.addText(t.name, {
      x: x + 0.2, y: y + 0.1, w: 4.1, h: 0.38,
      fontSize: 14, bold: true, color: t.color,
      fontFace: "Consolas", margin: 0
    });
    s.addText(t.desc, {
      x: x + 0.2, y: y + 0.5, w: 4.1, h: 0.6,
      fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 5 — Architecture MVC
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "03 — ARCHITECTURE");
  addTitle(s, "Architecture MVC", "Séparation stricte des responsabilités");

  // 3 colonnes MVC
  const cols = [
    { title: "Modèle",      color: C.accent, x: 0.4,  items: ["BombermanModel", "Player", "Bomb", "TileType", "ItemType", "GameSettings"] },
    { title: "Vue",         color: C.blue,   x: 3.65, items: ["BombermanView", "GamePanel"] },
    { title: "Contrôleur",  color: C.green,  x: 6.9,  items: ["GameController", "BotAI"] },
  ];

  cols.forEach(col => {
    addCard(s, col.x, 1.45, 3.0, 3.7);
    // Header coloré
    s.addShape(pres.shapes.RECTANGLE, {
      x: col.x, y: 1.45, w: 3.0, h: 0.52,
      fill: { color: col.color }, line: { color: col.color }
    });
    s.addText(col.title, {
      x: col.x, y: 1.45, w: 3.0, h: 0.52,
      fontSize: 18, bold: true, color: C.dark,
      fontFace: "Calibri", align: "center", margin: 0
    });
    col.items.forEach((item, i) => {
      s.addShape(pres.shapes.RECTANGLE, {
        x: col.x + 0.15, y: 2.1 + i * 0.52, w: 2.7, h: 0.4,
        fill: { color: C.navy }, line: { color: C.border, pt: 1 }
      });
      s.addText(item, {
        x: col.x + 0.15, y: 2.1 + i * 0.52, w: 2.7, h: 0.4,
        fontSize: 12, color: col.color, fontFace: "Consolas",
        align: "center", margin: 0
      });
    });
  });

  // Flèches entre colonnes
  s.addShape(pres.shapes.LINE, {
    x: 3.42, y: 3.3, w: 0.2, h: 0,
    line: { color: C.muted, pt: 1.5, dashType: "dash" }
  });
  s.addShape(pres.shapes.LINE, {
    x: 6.67, y: 3.3, w: 0.2, h: 0,
    line: { color: C.muted, pt: 1.5, dashType: "dash" }
  });

  // Légende en bas
  s.addText("Le Modèle ne connaît pas la Vue. La Vue observe le Modèle. Le Contrôleur orchestre les deux.", {
    x: 0.4, y: 5.2, w: 9.2, h: 0.3,
    fontSize: 11, color: C.muted, fontFace: "Calibri", align: "center", margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 6 — Structure des packages
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "04 — STRUCTURE");
  addTitle(s, "Structure des packages", "Organisation du code source");

  // Arbre de fichiers style terminal
  addCard(s, 0.4, 1.45, 5.5, 3.8);
  s.addText("📁 src/", {
    x: 0.6, y: 1.6, w: 5.1, h: 0.38,
    fontSize: 14, bold: true, color: C.accent, fontFace: "Consolas", margin: 0
  });

  const tree = [
    { text: "├── model/",            indent: 0.4, color: C.blue   },
    { text: "│   ├── BombermanModel.java",  indent: 0.8, color: C.light  },
    { text: "│   ├── Player.java",          indent: 0.8, color: C.light  },
    { text: "│   ├── Bomb.java",            indent: 0.8, color: C.light  },
    { text: "│   ├── TileType.java",        indent: 0.8, color: C.light  },
    { text: "│   ├── ItemType.java",        indent: 0.8, color: C.light  },
    { text: "│   └── GameSettings.java",    indent: 0.8, color: C.light  },
    { text: "├── View/",             indent: 0.4, color: C.green  },
    { text: "│   ├── BombermanView.java",   indent: 0.8, color: C.light  },
    { text: "│   └── GamePanel.java",       indent: 0.8, color: C.light  },
    { text: "└── controller/",       indent: 0.4, color: C.red    },
    { text: "    ├── GameController.java",  indent: 0.8, color: C.light  },
    { text: "    └── BotAI.java",           indent: 0.8, color: C.light  },
  ];

  tree.forEach((line, i) => {
    s.addText(line.text, {
      x: 0.6 + line.indent, y: 2.05 + i * 0.25, w: 4.8, h: 0.26,
      fontSize: 11, color: line.color, fontFace: "Consolas", margin: 0
    });
  });

  // Carte droite — rôles
  addCard(s, 6.1, 1.45, 3.5, 3.8);
  s.addText("Rôles", {
    x: 6.3, y: 1.6, w: 3.1, h: 0.38,
    fontSize: 14, bold: true, color: C.accent, fontFace: "Calibri", margin: 0
  });

  const roles = [
    { pkg: "model/",      role: "Logique de jeu pure,\naucune dépendance UI",   color: C.blue  },
    { pkg: "View/",       role: "Rendu graphique Swing,\nlecture seule du modèle", color: C.green },
    { pkg: "controller/", role: "Boucle de jeu, inputs,\nIA du bot",            color: C.red   },
    { pkg: "Main.java",   role: "Point d'entrée,\nmenu de sélection",           color: C.accent},
  ];

  roles.forEach((r, i) => {
    const y = 2.1 + i * 0.82;
    s.addShape(pres.shapes.RECTANGLE, {
      x: 6.1, y, w: 0.06, h: 0.62,
      fill: { color: r.color }, line: { color: r.color }
    });
    s.addText(r.pkg, {
      x: 6.25, y, w: 3.1, h: 0.28,
      fontSize: 11, bold: true, color: r.color, fontFace: "Consolas", margin: 0
    });
    s.addText(r.role, {
      x: 6.25, y: y + 0.28, w: 3.1, h: 0.36,
      fontSize: 10, color: C.muted, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 7 — Modèle
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "05 — MODÈLE");
  addTitle(s, "BombermanModel — logique de jeu", "Le cœur du projet : état complet de la partie");

  const features = [
    { icon: "🗺️", title: "Grille de jeu",       desc: "TileType[][] de 15×15. Génération procédurale avec murs fixes (piliers), blocs destructibles (65%), zones de spawn protégées." },
    { icon: "💣", title: "Gestion des bombes",   desc: "Liste d'objets Bomb actifs. Explosion en croix avec portée variable, réaction en chaîne, durée configurable." },
    { icon: "🔥", title: "Système d'explosions", desc: "Map<Long, Long> activeExplosions : clé = position encodée, valeur = timestamp d'expiration. Fusion correcte des flammes superposées." },
    { icon: "🎮", title: "Joueurs & items",       desc: "2 instances Player. 4 types d'items : vitesse+/-, portée+, bombe+. Ramassage automatique au mouvement." },
    { icon: "🔄", title: "Boucle update()",       desc: "Appelée à chaque tick : expiration flammes → déclenchement bombes → dégâts joueurs → condition de victoire." },
    { icon: "⚙️", title: "GameSettings",          desc: "Configuration centralisée : dimensions carte, vitesse jeu, durée explosion, probabilité de drop d'items." },
  ];

  features.forEach((f, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.4 + col * 4.85;
    const y = 1.45 + row * 1.38;
    addCard(s, x, y, 4.55, 1.25);
    s.addText(f.icon + " " + f.title, {
      x: x + 0.15, y: y + 0.1, w: 4.2, h: 0.38,
      fontSize: 13, bold: true, color: C.accent, fontFace: "Calibri", margin: 0
    });
    s.addText(f.desc, {
      x: x + 0.15, y: y + 0.5, w: 4.2, h: 0.65,
      fontSize: 10.5, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 8 — Vue
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "06 — VUE");
  addTitle(s, "GamePanel — rendu graphique", "Rendu 2D entièrement custom avec Java2D");

  // Colonne gauche : liste des éléments rendus
  addCard(s, 0.4, 1.45, 5.4, 3.7);
  addAccentBar(s, 0.4, 1.45, 3.7, C.blue);
  s.addText("Éléments dessinés", {
    x: 0.6, y: 1.55, w: 5.0, h: 0.4,
    fontSize: 15, bold: true, color: C.blue, fontFace: "Calibri", margin: 0
  });

  const elements = [
    { name: "Grille",       desc: "Damier alternant FLOOR_A/B, murs en dégradé, blocs en bois stylisé" },
    { name: "Explosions",   desc: "Animation pulsante 3 couches (EXPL_OUTER → MID → CORE)" },
    { name: "Bombes",       desc: "Ombre portée, dégradé sphérique, mèche clignotante + compte à rebours" },
    { name: "Joueurs",      desc: "Cercle avec glow, reflet, visage (yeux + sourire). Bot = capteurs verts" },
    { name: "Items",        desc: "Pastilles colorées avec label : ▲V ▼V +B ▲R" },
    { name: "Stats",        desc: "Panneaux latéraux : vies (cœurs), barres vitesse/portée/bombes" },
    { name: "Scanlines",    desc: "Overlay rétro semi-transparent mis en cache (BufferedImage)" },
    { name: "Game Over",    desc: "Overlay sombre + bouton PLAY AGAIN cliquable" },
  ];

  elements.forEach((el, i) => {
    s.addText("▸ " + el.name + " :", {
      x: 0.6, y: 2.05 + i * 0.38, w: 1.4, h: 0.32,
      fontSize: 11, bold: true, color: C.blue, fontFace: "Calibri", margin: 0
    });
    s.addText(el.desc, {
      x: 2.05, y: 2.05 + i * 0.38, w: 3.55, h: 0.32,
      fontSize: 10.5, color: C.light, fontFace: "Calibri", margin: 0
    });
  });

  // Colonne droite : techniques clés
  addCard(s, 6.0, 1.45, 3.6, 3.7);
  s.addText("Techniques Java2D", {
    x: 6.2, y: 1.55, w: 3.2, h: 0.4,
    fontSize: 15, bold: true, color: C.red, fontFace: "Calibri", margin: 0
  });

  const techs2D = [
    "Graphics2D + RenderingHints",
    "GradientPaint pour dégradés",
    "AlphaComposite pour transparences",
    "RoundRectangle2D pour boutons",
    "BufferedImage pour cache scanlines",
    "repaint() déclenché par le Timer",
    "TILE_SIZE = 44px (coordinées pixel)",
    "getPixelX/Y() pour rendu fluide",
  ];
  techs2D.forEach((t, i) => {
    s.addText("• " + t, {
      x: 6.2, y: 2.1 + i * 0.37, w: 3.2, h: 0.3,
      fontSize: 11, color: i % 2 === 0 ? C.light : C.muted,
      fontFace: "Consolas", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 9 — Contrôleur
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "07 — CONTRÔLEUR");
  addTitle(s, "GameController — boucle de jeu", "Orchestration entrées → logique → rendu");

  // Diagramme boucle de jeu
  addCard(s, 0.4, 1.45, 9.2, 1.5);
  s.addText("Boucle de jeu (toutes les 30ms via javax.swing.Timer)", {
    x: 0.6, y: 1.55, w: 8.8, h: 0.35,
    fontSize: 11, color: C.muted, fontFace: "Calibri", margin: 0
  });

  const steps = [
    { label: "Restart ?",           color: C.red    },
    { label: "placeBomb flags",     color: C.accent },
    { label: "movePlayer()",        color: C.blue   },
    { label: "handleBotTurn()",     color: C.green  },
    { label: "model.update()",      color: C.accent },
    { label: "view.refresh()",      color: C.blue   },
  ];
  steps.forEach((step, i) => {
    const bx = 0.55 + i * 1.52;
    s.addShape(pres.shapes.RECTANGLE, {
      x: bx, y: 2.0, w: 1.35, h: 0.68,
      fill: { color: step.color }, line: { color: step.color }
    });
    s.addText(step.label, {
      x: bx, y: 2.0, w: 1.35, h: 0.68,
      fontSize: 10, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    if (i < steps.length - 1) {
      s.addShape(pres.shapes.LINE, {
        x: bx + 1.35, y: 2.34, w: 0.17, h: 0,
        line: { color: C.muted, pt: 1.5 }
      });
    }
  });

  // 2 cartes en bas
  addCard(s, 0.4, 3.1, 4.5, 2.15);
  addAccentBar(s, 0.4, 3.1, 2.15, C.accent);
  s.addText("Gestion des touches", {
    x: 0.6, y: 3.2, w: 4.1, h: 0.38,
    fontSize: 14, bold: true, color: C.accent, fontFace: "Calibri", margin: 0
  });
  s.addText([
    { text: "keyPressed() → ajoute dans pressedKeys (Set<Integer>)", options: { breakLine: true } },
    { text: "keyReleased() → retire de pressedKeys", options: { breakLine: true } },
    { text: "Bombes via flags booléens placeBomb1/2", options: { breakLine: true } },
    { text: "Lecture des flags dans tick() → thread-safe", options: {} },
  ], {
    x: 0.6, y: 3.65, w: 4.1, h: 1.45,
    fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
  });

  addCard(s, 5.1, 3.1, 4.5, 2.15);
  addAccentBar(s, 5.1, 3.1, 2.15, C.green);
  s.addText("Modes de jeu", {
    x: 5.3, y: 3.2, w: 4.1, h: 0.38,
    fontSize: 14, bold: true, color: C.green, fontFace: "Calibri", margin: 0
  });
  s.addText([
    { text: "2 joueurs humains : P1 (flèches+Entrée), P2 (WASD+Espace)", options: { breakLine: true } },
    { text: "Humain vs IA : P2 géré par BotAI selon difficulté choisie", options: { breakLine: true } },
    { text: "R = restart, ESC = quitter en cours de partie", options: {} },
  ], {
    x: 5.3, y: 3.65, w: 4.1, h: 1.45,
    fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 10 — IA
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "08 — INTELLIGENCE ARTIFICIELLE");
  addTitle(s, "BotAI — le joueur artificiel", "3 niveaux de difficulté, comportements adaptatifs");

  // Carte principale — arbre de décision
  addCard(s, 0.4, 1.45, 9.2, 1.55);
  s.addText("Arbre de décision par tick (tickCooldown contrôle la fréquence de décision)", {
    x: 0.6, y: 1.55, w: 8.8, h: 0.3,
    fontSize: 10, color: C.muted, fontFace: "Calibri", margin: 0
  });

  const decisions = [
    { q: "En danger ?",        a: "Fuite / bombe ultime",  color: C.red    },
    { q: "Peut poser bombe ?", a: "Si survie assurée",     color: C.accent },
    { q: "Humain visible ?",   a: "BFS vers humain",       color: C.blue   },
    { q: "Sinon",              a: "Marche aléatoire",      color: C.green  },
  ];
  decisions.forEach((d, i) => {
    const bx = 0.55 + i * 2.3;
    s.addShape(pres.shapes.RECTANGLE, {
      x: bx, y: 1.95, w: 2.1, h: 0.35,
      fill: { color: C.navy }, line: { color: d.color, pt: 1 }
    });
    s.addText(d.q, {
      x: bx, y: 1.95, w: 2.1, h: 0.35,
      fontSize: 10, color: d.color, fontFace: "Consolas",
      align: "center", margin: 0
    });
    s.addShape(pres.shapes.LINE, {
      x: bx + 1.05, y: 2.3, w: 0, h: 0.22,
      line: { color: d.color, pt: 1 }
    });
    s.addShape(pres.shapes.RECTANGLE, {
      x: bx, y: 2.52, w: 2.1, h: 0.35,
      fill: { color: d.color }, line: { color: d.color }
    });
    s.addText(d.a, {
      x: bx, y: 2.52, w: 2.1, h: 0.35,
      fontSize: 10, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
  });

  // 3 cartes niveaux
  const levels = [
    { name: "EASY",   color: C.green,  cooldown: "6–12 ticks", bombProb: "30%",  desc: "Réaction lente et aléatoire. Pose rarement des bombes. Facile à éviter." },
    { name: "MEDIUM", color: C.accent, cooldown: "3–6 ticks",  bombProb: "60%",  desc: "Équilibré. BFS actif, fuite correcte. Bon adversaire pour débuter." },
    { name: "HARD",   color: C.red,    cooldown: "1 tick",     bombProb: "100%", desc: "Décision à chaque tick. Pose toujours la bombe si c'est sûr. Très agressif." },
  ];
  levels.forEach((lv, i) => {
    const x = 0.4 + i * 3.15;
    addCard(s, x, 3.1, 2.95, 2.2);
    s.addShape(pres.shapes.RECTANGLE, {
      x, y: 3.1, w: 2.95, h: 0.45,
      fill: { color: lv.color }, line: { color: lv.color }
    });
    s.addText(lv.name, {
      x, y: 3.1, w: 2.95, h: 0.45,
      fontSize: 16, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    s.addText("Cooldown : " + lv.cooldown, {
      x: x + 0.15, y: 3.65, w: 2.65, h: 0.3,
      fontSize: 11, color: lv.color, fontFace: "Consolas", margin: 0
    });
    s.addText("Probabilité bombe : " + lv.bombProb, {
      x: x + 0.15, y: 3.95, w: 2.65, h: 0.3,
      fontSize: 11, color: lv.color, fontFace: "Consolas", margin: 0
    });
    s.addText(lv.desc, {
      x: x + 0.15, y: 4.3, w: 2.65, h: 0.85,
      fontSize: 10, color: C.light, fontFace: "Calibri", margin: 0
    });
  });

  // Algorithmes
  addCard(s, 9.55, 3.1, 0, 0); // pas de 4e carte, on met une note
  s.addText("Algorithmes : BFS (pathfinding), Manhattan distance (détection), hasSafeEscape (simulation blast zone)", {
    x: 0.4, y: 5.25, w: 9.2, h: 0.28,
    fontSize: 10, color: C.muted, fontFace: "Consolas", align: "center", margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 11 — Mouvement pixel
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "09 — MOUVEMENT PIXEL");
  addTitle(s, "Mouvement pixel fluide", "Passage de tile-by-tile à un système continu en pixels");

  // Avant / Après
  addCard(s, 0.4, 1.45, 4.4, 3.8);
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.4, y: 1.45, w: 4.4, h: 0.45,
    fill: { color: C.red }, line: { color: C.red }
  });
  s.addText("❌  Avant (tile-by-tile)", {
    x: 0.4, y: 1.45, w: 4.4, h: 0.45,
    fontSize: 13, bold: true, color: C.white,
    fontFace: "Calibri", align: "center", margin: 0
  });
  const before = [
    "Position : int x, y (indices tile)",
    "moveDelay : cooldown en millisecondes",
    "canMove() vérifie System.currentTimeMillis()",
    "movePlayer() : déplace d'une tile entière",
    "Rendu : getX() * TILE_SIZE",
    "Résultat : saccadé, peu réactif",
  ];
  before.forEach((b, i) => {
    s.addText("• " + b, {
      x: 0.6, y: 2.02 + i * 0.51, w: 4.0, h: 0.44,
      fontSize: 11, color: i === 5 ? C.red : C.light,
      fontFace: "Calibri", bold: i === 5, margin: 0
    });
  });

  addCard(s, 5.2, 1.45, 4.4, 3.8);
  s.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.45, w: 4.4, h: 0.45,
    fill: { color: C.green }, line: { color: C.green }
  });
  s.addText("✅  Après (pixel fluide)", {
    x: 5.2, y: 1.45, w: 4.4, h: 0.45,
    fontSize: 13, bold: true, color: C.dark,
    fontFace: "Calibri", align: "center", margin: 0
  });
  const after = [
    "Position : float px, py (pixels)",
    "speed : pixels/tick (défaut 2.2f)",
    "canMove() → true si alive",
    "moveBy() : déplacement sub-tile + collision",
    "Rendu : getPixelX/Y()",
    "Résultat : mouvement fluide et réactif",
  ];
  after.forEach((a, i) => {
    s.addText("• " + a, {
      x: 5.4, y: 2.02 + i * 0.51, w: 4.0, h: 0.44,
      fontSize: 11, color: i === 5 ? C.green : C.light,
      fontFace: "Calibri", bold: i === 5, margin: 0
    });
  });

  // Note collision
  s.addText("Collision : hitbox 4 coins (Math.floor) · Axis separation (sliding le long des murs) · Le joueur traverse ses propres bombes (b.getOwner() != this)", {
    x: 0.4, y: 5.22, w: 9.2, h: 0.3,
    fontSize: 10, color: C.muted, fontFace: "Consolas", align: "center", margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 12 — Items & Bombes
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "10 — ITEMS & BOMBES");
  addTitle(s, "Système d'items & bombes", "Power-ups, malus et explosions en chaîne");

  // Items
  addCard(s, 0.4, 1.45, 4.55, 3.8);
  addAccentBar(s, 0.4, 1.45, 3.8, C.accent);
  s.addText("Items (drop sur bloc détruit)", {
    x: 0.6, y: 1.55, w: 4.15, h: 0.38,
    fontSize: 14, bold: true, color: C.accent, fontFace: "Calibri", margin: 0
  });

  const items = [
    { icon: "▲V", name: "BONUS_SPEED",      desc: "speed += 0.4f (max 4.5)",    color: "3CDCFF" },
    { icon: "▼V", name: "MALUS_SLOW",       desc: "speed -= 0.5f (min 1.4)",    color: "B028C8" },
    { icon: "+B", name: "BONUS_BOMB_COUNT", desc: "bombCapacity++ (max 8)",      color: "FFC828" },
    { icon: "▲R", name: "BONUS_RANGE",      desc: "bombRange++ (max 9)",         color: "FF7828" },
  ];
  items.forEach((item, i) => {
    const y = 2.05 + i * 0.78;
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.6, y, w: 0.55, h: 0.55,
      fill: { color: item.color }, line: { color: item.color }
    });
    s.addText(item.icon, {
      x: 0.6, y, w: 0.55, h: 0.55,
      fontSize: 13, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    s.addText(item.name, {
      x: 1.25, y, w: 3.5, h: 0.28,
      fontSize: 12, bold: true, color: C.light, fontFace: "Consolas", margin: 0
    });
    s.addText(item.desc, {
      x: 1.25, y: y + 0.28, w: 3.5, h: 0.28,
      fontSize: 10, color: C.muted, fontFace: "Calibri", margin: 0
    });
  });
  s.addText("Probabilité de drop configurable (défaut 35%)\nRamassage automatique dans movePlayer()", {
    x: 0.6, y: 5.05, w: 4.0, h: 0.5,
    fontSize: 10, color: C.muted, fontFace: "Calibri", margin: 0
  });

  // Bombes
  addCard(s, 5.15, 1.45, 4.45, 3.8);
  addAccentBar(s, 5.15, 1.45, 3.8, C.red);
  s.addText("Cycle de vie d'une bombe", {
    x: 5.35, y: 1.55, w: 4.05, h: 0.38,
    fontSize: 14, bold: true, color: C.red, fontFace: "Calibri", margin: 0
  });

  const bombSteps = [
    { step: "1", label: "Pose",        desc: "Bomb(x, y, range, owner) ajoutée à activeBombs. onBombPlaced() incrémente le compteur." },
    { step: "2", label: "Décompte",    desc: "2500ms avant explosion. getRemainingMs() affiché. Mèche clignotante dans GamePanel." },
    { step: "3", label: "Explosion",   desc: "triggerExplosion() : croix dans 4 directions, arrêt sur WALL, destruction de DESTRUCTIBLE_BLOCK." },
    { step: "4", label: "Flamme",      desc: "applyExplosionCell() : grid = EXPLOSION, activeExplosions map mis à jour, dégâts immédiats." },
    { step: "5", label: "Expiration",  desc: "expireExplosions() nettoie après explosionDurationMs (800ms). Case redevient EMPTY ou ITEM." },
  ];
  bombSteps.forEach((bs, i) => {
    const y = 2.05 + i * 0.65;
    s.addShape(pres.shapes.RECTANGLE, {
      x: 5.35, y, w: 0.38, h: 0.38,
      fill: { color: C.red }, line: { color: C.red }
    });
    s.addText(bs.step, {
      x: 5.35, y, w: 0.38, h: 0.38,
      fontSize: 13, bold: true, color: C.dark,
      fontFace: "Consolas", align: "center", margin: 0
    });
    s.addText(bs.label, {
      x: 5.83, y, w: 3.55, h: 0.2,
      fontSize: 11, bold: true, color: C.red, fontFace: "Calibri", margin: 0
    });
    s.addText(bs.desc, {
      x: 5.83, y: y + 0.2, w: 3.55, h: 0.38,
      fontSize: 9.5, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 13 — Collisions
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "11 — COLLISIONS");
  addTitle(s, "Gestion des collisions", "Hitbox pixel-parfaite et cas particuliers");

  const topics = [
    {
      title: "Hitbox 4 coins",
      color: C.blue,
      desc: "On teste les 4 coins de la hitbox (centre ± half) avec Math.floor() pour convertir en tile. half = TILE_SIZE/2 - 4px (marge anti-blocage dans les angles)."
    },
    {
      title: "Axis separation",
      color: C.green,
      desc: "On teste X et Y indépendamment. Si X bloqué mais Y libre : on bouge en Y et on snape vers le centre de tile sur X (glissement le long des murs)."
    },
    {
      title: "Bombes propres",
      color: C.accent,
      desc: "b.getOwner() != this : le joueur traverse toujours ses propres bombes. Solution simple et robuste qui évite tout blocage post-pose."
    },
    {
      title: "Flammes & dégâts",
      color: C.red,
      desc: "isFlameActive() vérifie la Map activeExplosions. Dégâts dans movePlayer() (entrée dans flamme) ET dans damagePlayersOnFlames() (tick). Invincibilité 2s post-hit."
    },
    {
      title: "Snap axis",
      color: C.green,
      desc: "Quand bloqué sur un axe, snapAxis() pousse doucement le joueur vers le centre de sa tile courante (speed × 0.5f) pour faciliter le passage dans les couloirs."
    },
    {
      title: "Bombes en chaîne",
      color: C.accent,
      desc: "applyExplosionCell() détecte toute bombe sur la case enflammée et la déclenche récursivement → explosions en cascade réalistes."
    },
  ];

  topics.forEach((t, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.4 + col * 4.85;
    const y = 1.45 + row * 1.38;
    addCard(s, x, y, 4.55, 1.25);
    addAccentBar(s, x, y, 1.25, t.color);
    s.addText(t.title, {
      x: x + 0.2, y: y + 0.1, w: 4.15, h: 0.35,
      fontSize: 13, bold: true, color: t.color, fontFace: "Calibri", margin: 0
    });
    s.addText(t.desc, {
      x: x + 0.2, y: y + 0.5, w: 4.15, h: 0.65,
      fontSize: 10.5, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 14 — Défis & solutions
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };
  addSlideHeader(s, "12 — DÉFIS & SOLUTIONS");
  addTitle(s, "Principaux défis rencontrés", "Problèmes techniques et solutions adoptées");

  const challenges = [
    {
      problem: "Flammes superposées",
      solution: "Map<Long, Long> avec expiry maximum au lieu d'une liste → fusion correcte, pas de nettoyage prématuré.",
      color: C.red
    },
    {
      problem: "Joueur bloqué par sa bombe",
      solution: "b.getOwner() != this dans canMoveTo() → le joueur traverse toujours ses propres bombes, simple et sans état.",
      color: C.accent
    },
    {
      problem: "Mouvement saccadé tile-by-tile",
      solution: "Positions float px/py + moveBy() avec hitbox 4 coins + axis separation pour le sliding.",
      color: C.blue
    },
    {
      problem: "Race condition EDT / Timer",
      solution: "placeBomb via flags booléens lus dans tick() → toutes les mutations modèle dans le même thread.",
      color: C.green
    },
    {
      problem: "IA se suicide avec ses bombes",
      solution: "hasSafeEscape() simule la blast zone avant de poser + tickCooldown adapté à la difficulté.",
      color: C.red
    },
  ];

  challenges.forEach((c, i) => {
    const y = 1.45 + i * 0.82;
    addCard(s, 0.4, y, 9.2, 0.72);
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.4, y, w: 0.06, h: 0.72,
      fill: { color: c.color }, line: { color: c.color }
    });
    s.addText("⚠ " + c.problem, {
      x: 0.6, y: y + 0.05, w: 3.8, h: 0.3,
      fontSize: 12, bold: true, color: c.color, fontFace: "Calibri", margin: 0
    });
    s.addText("→ " + c.solution, {
      x: 0.6, y: y + 0.36, w: 8.8, h: 0.3,
      fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 15 — Conclusion
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide();
  s.background = { color: C.dark };

  s.addShape(pres.shapes.RECTANGLE, {
    x: 0, y: 0, w: 0.18, h: 5.625,
    fill: { color: C.accent }, line: { color: C.accent }
  });

  addSlideHeader(s, "13 — CONCLUSION");
  addTitle(s, "Bilan & perspectives", null, 0.45);

  // Acquis
  addCard(s, 0.4, 1.35, 4.4, 2.5);
  addAccentBar(s, 0.4, 1.35, 2.5, C.green);
  s.addText("✅  Ce que le projet apporte", {
    x: 0.6, y: 1.45, w: 4.0, h: 0.38,
    fontSize: 13, bold: true, color: C.green, fontFace: "Calibri", margin: 0
  });
  const acquis = [
    "Architecture MVC propre et extensible",
    "Rendu Java2D performant sans librairie",
    "IA avec BFS, fuite et évaluation de risque",
    "Mouvement pixel fluide avec collisions robustes",
    "Boucle de jeu temps-réel avec Swing Timer",
    "Gestion d'état complexe (bombes, flammes, items)",
  ];
  acquis.forEach((a, i) => {
    s.addText("• " + a, {
      x: 0.6, y: 1.92 + i * 0.3, w: 4.0, h: 0.28,
      fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
    });
  });

  // Perspectives
  addCard(s, 5.2, 1.35, 4.4, 2.5);
  addAccentBar(s, 5.2, 1.35, 2.5, C.blue);
  s.addText("🚀  Améliorations possibles", {
    x: 5.4, y: 1.45, w: 4.0, h: 0.38,
    fontSize: 13, bold: true, color: C.blue, fontFace: "Calibri", margin: 0
  });
  const persp = [
    "Mode réseau (multijoueur en ligne)",
    "Sprites animés (spritesheet)",
    "Éditeur de niveaux",
    "IA améliorée (A*, Monte Carlo)",
    "Scores & classement persistant",
    "Support manette (GamePad API)",
  ];
  persp.forEach((p, i) => {
    s.addText("• " + p, {
      x: 5.4, y: 1.92 + i * 0.3, w: 4.0, h: 0.28,
      fontSize: 11, color: C.light, fontFace: "Calibri", margin: 0
    });
  });

  // Mot de fin
  addCard(s, 0.4, 4.0, 9.2, 1.1);
  s.addText("Un projet complet qui couvre les fondamentaux du développement Java : conception objet, patron MVC, rendu 2D, algorithmes de jeu et intelligence artificielle.", {
    x: 0.6, y: 4.1, w: 8.8, h: 0.85,
    fontSize: 13, color: C.light, fontFace: "Calibri",
    align: "center", valign: "middle", margin: 0
  });

  s.addText("💣  Merci pour votre attention", {
    x: 0.4, y: 5.22, w: 9.2, h: 0.3,
    fontSize: 12, bold: true, color: C.accent,
    fontFace: "Calibri", align: "center", margin: 0
  });
}

// ── Export ────────────────────────────────────────────────────────────────────
pres.writeFile({ fileName: "/mnt/user-data/outputs/Bomberman_Presentation.pptx" })
  .then(() => console.log("OK"))
  .catch(e => { console.error(e); process.exit(1); });
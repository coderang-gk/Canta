import { defineConfig } from "vitepress";

// Custom color theme
const cantaRed = {
  primary: "#ff4444",
  secondary: "#ff6b6b",
  accent: "#ff0000",
  foreground: "#333333",
  background: "#ffffff",

  // Dark mode variants
  darkPrimary: "#ff6666",
  darkSecondary: "#ff8c8c",
  darkAccent: "#ff3333",
  darkForeground: "#ffffff",
  darkBackground: "#121212",
};

export default defineConfig({
  title: "Canta",
  description: "Uninstall any app without root!",
  head: [
    [
      "link",
      {
        rel: "icon",
        href: "https://raw.githubusercontent.com/samolego/Canta/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
      },
    ],
  ],

  // Theme customization
  themeConfig: {
    logo: "https://raw.githubusercontent.com/samolego/Canta/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
    nav: [
      { text: "Home", link: "/" },
      { text: "Guide", link: "/guide" },
      { text: "Download", link: "/download" },
    ],

    sidebar: [
      {
        text: "Guide",
        items: [
          { text: "Getting Started", link: "/guide" },
          { text: "Features", link: "/features" },
          { text: "Verification", link: "/verification" },
        ],
      },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/samolego/Canta" },
    ],

    footer: {
      message: "Released under the GPL-3.0 License.",
      copyright: "Copyright © 2023-present Samo Lego",
    },
  },

  // CSS customization
  appearance: "dark",

  // Theme colors
  vite: {
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `
            :root {
              --vp-c-brand: ${cantaRed.primary};
              --vp-c-brand-light: ${cantaRed.secondary};
              --vp-c-brand-lighter: ${cantaRed.accent};
              --vp-c-brand-dark: ${cantaRed.darkPrimary};
              --vp-c-brand-darker: ${cantaRed.darkSecondary};

              --vp-home-hero-name-color: ${cantaRed.primary};
              --vp-c-text-1: ${cantaRed.foreground};
              --vp-c-text-2: ${cantaRed.foreground};
            }

            .dark {
              --vp-c-brand: ${cantaRed.darkPrimary};
              --vp-c-brand-light: ${cantaRed.darkSecondary};
              --vp-c-brand-lighter: ${cantaRed.darkAccent};
              --vp-c-brand-dark: ${cantaRed.primary};
              --vp-c-brand-darker: ${cantaRed.secondary};

              --vp-home-hero-name-color: ${cantaRed.darkPrimary};
              --vp-c-text-1: ${cantaRed.darkForeground};
              --vp-c-text-2: ${cantaRed.darkForeground};
            }
          `,
        },
      },
    },
  },
});

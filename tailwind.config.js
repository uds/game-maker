/** @type {import('tailwindcss').Config} */ 

const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
  content: ['src/game_maker/htmx/**/*.cljs'],
  plugins: [
    require('@tailwindcss/aspect-ratio'),
    require('@tailwindcss/forms')({
      strategy: 'class'   // do not apply global classes, form element should explicitly use "form-{name}" class
    })
  ]
}

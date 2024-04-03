/** @type {import('tailwindcss').Config} */ 

module.exports = {
  content: ['src/game_maker/views/**/*.cljs'],
  plugins: [
    require('@tailwindcss/aspect-ratio'),
    require('@tailwindcss/forms')({
      strategy: 'class'   // do not apply global classes, form element should explicitly use "form-{name}" class
    })
  ]
}

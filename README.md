# Game Maker

## Description
Game Maker is a project that explores the use of LLM model to generate code for a simple game using a specialized API. The project is written in ClojureScript and uses shadow-cljs for building. It is hosted on a Node.js server and utilizes HTMX for rendering in the browser. The game engine used in this project is Babylon.js.

## Setup
To set up the project, follow the steps below:

1. Clone the repository to your local machine.
2. Install Node.js if you haven't already.
3. Open a terminal and navigate to the project directory.
4. Run the following command to install the project dependencies:
   ```
   npm install

## Development

### Compilation of CSS resources 
TailwindCSS styles can be recompiled manually by running following command:  
`npm run tailwindcss-dev` or `npm run tailwindcss-prod`
Note that appropriate `tailwindcss-...` task is NOT run automatically by shadow-cljs (e.g. by Calva jack-in). 
One should run `npm run watch` or `npm run release` to rebuild everything.

To ran TailwindCSS compiler in a watch mode while developing:  
`npm run tailwindcss-watch`  
or  
`npx tailwindcss -i src/app/phs/front/styles.css -o resources/public/css/compiled/front-styles.css --watch`

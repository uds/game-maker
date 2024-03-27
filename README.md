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
   ```

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


### Calva Jack-in

To start the Clojure REPL and connect to it using Calva, follow these steps:

1. Open Visual Studio Code.
2. Open the project directory in Visual Studio Code.
3. Install the Calva extension if you haven't already.
4. Press `Ctrl+Alt+C` or go to the Command Palette (`Ctrl+Shift+P`).
5. Search for "Calva: Jack-in" and select the appropriate option for your project setup.
6. Calva will start the Clojure REPL and connect to it.

### Start node server

``` 
npm run server
```

## Prompt examples

Create canvas surrounded by green walls.
Create a line of 4 yellow bricks at top center of canvas, with interval between bricks is 0.3.
Create red ball in center.
Move yellow bricks to the left by 5.

---

Create canvas surrounded by green walls.
Create a line of 4 yellow bricks starting at top left corner of the canvas, with interval between bricks is 0.3.
Create red ball in center.

---

Create canvas surrounded by green walls.
Create a line of 4 orange bricks at the top of the canvas, stretched between the left and right walls so that:  the interval between bricks is 0.3 and the margin to the top wall is 2.
Create red ball in center.

---

Create a thin floor plate with size 20 on 20.
Create 50 balls with different color each, floating over the floor at random positions and heights.

package app;

// SDL imports
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.video.SDL_Window;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.*;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_ESCAPE;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateRenderer;
import static io.github.libsdl4j.api.render.SdlRender.SDL_DestroyRenderer;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_DestroyWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import app.Logger.Severity;

/**
 * The Engine
 */
public class Engine {
    private static SDL_Window window;
    private static SDL_Renderer renderer;
    private static boolean running = true;
    private static Vf2 winSize = new Vf2(1920*0.8f, 1080*0.8f);

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static Logger log;

    /**
     * Should only be called once before running the program<br>
     * Initializes all necessary variables
     */
    public void init() {
        // Create logger and start its thread
        try {
            log = new Logger();
            log.init();
            executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    log.run();
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("\u001B[31m[Warning] Could not create logger. Logs won't be saved!\u001B[0m");
            log = null;
        }

        // Initialize SDL
        int result = SDL_Init(SDL_INIT_EVERYTHING);
        if (result != 0) {
            Logger.log(Severity.EXCEPTION, "Engine", "Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError());
            running = false;
            return;
        }

        // Create and init the window
        window = SDL_CreateWindow("Demo SDL2", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, (int)winSize.x, (int)winSize.y, SDL_WINDOW_SHOWN);
        if (window == null) {
            Logger.log(Severity.EXCEPTION, "Engine", "Unable to create SDL window: " + SDL_GetError());
            running = false;
            return;
        }

        // Create and init the renderer
        renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
        if (renderer == null) {
            Logger.log(Severity.EXCEPTION, "Engine", "Unable to create SDL renderer: " + SDL_GetError());
            running = false;
            return;
        }

        // Initialization and loading
        DrawHandler.init(renderer);
        DrawHandler.load();

        TextHandler.init(renderer);
        Ui.Init();
        Menu.init();

        ObjectHandler.loadObjects("data/obj.data");

        // Camera setup
        Camera.setTarget(ObjectHandler.getObject("player").getId());
        Camera.setMode(Camera.Mode.singleTarget);
        Camera.jumpTarget();
    }

    /**
     * To use when exception happenes
     */
    public static void forceQuit() {
        if (log != null) {
            executor.shutdown();
            log.quit();
        }
        System.exit(1);
    }

    /**
     * Cleans everything up in preparation for exiting the program
     * Should only be called before exiting the program
     */
    private void quit() {
        SDL_DestroyWindow(window);
        SDL_DestroyRenderer(renderer);

        // Logger quit
        if (log != null) {
            executor.shutdown();
            log.quit();
        }

        // Uncommenting this causes seg fault and i don't know why
        // SDL_Quit();
    }

    /*
     * Handles all SDL events 
     */
    private void eventUpdate() {
        SDL_Event e = new SDL_Event();
        while (SDL_PollEvent(e) != 0) {
            switch (e.type) {
                case SDL_QUIT:
                    running = false;
                    break;
                case SDL_KEYDOWN:
                    if (e.key.keysym.sym == SDLK_ESCAPE) {
                        Menu.setState(!Menu.isRunning());
                    } else {
                        Input.setKeyDown(e.key.keysym);
                    }
                    break;
                case SDL_KEYUP:
                    Input.setKeyUp(e.key.keysym);
                case SDL_MOUSEBUTTONDOWN:
                    Input.setMouseDown(e.button.button);
                    break;
                case SDL_MOUSEBUTTONUP:
                    Input.setMouseUp(e.button.button);
                    break;
                case SDL_MOUSEMOTION:
                    Input.setMousePos(new Vd2(e.motion.x, e.motion.y));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Main game loop
     */
    public void run() {
        // Mixer.Play("running.wav");
        ObjectHandler.loadState();

        // Main application loop
        while (running) {
            Time.update();
            eventUpdate();

            DrawHandler.draw();

            if (!Menu.isRunning()) {
                Ui.draw();
            }
            
            ObjectHandler.update();
            ObjectHandler.remUpdate();
            ObjectHandler.addCreatedObjects();
            ObjectHandler.resetUpdate();
            Camera.update();

            TextHandler.alignRight();
            TextHandler.alignBottom();
            TextHandler.drawText("FPS: " + Time.getFps(), new Vd2(DrawHandler.getWinSize()), 4);

            TextHandler.alignLeft();
            TextHandler.alignTop();

            if (Menu.isRunning()) {
                Menu.update();
                Menu.draw();
            }

            DrawHandler.present();
        }

        quit();
        return;
    }
    
    public static Vf2 getWinSize() {
        return new Vf2(winSize);
    }

    /**
     * Should only be called from menu
     */
    public static void exit() {
        running = false;
    }
}

package com.boids;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class BoidSimulationScreen extends ScreenAdapter {

    private Boid[] boids;
    private ShapeRenderer renderer;
    private boolean useParallel = true;
    private BitmapFont font;
    private SpriteBatch batch;
    private long lastFrameTime; // tempo do último frame em milissegundos
    private float averageFps = 0;
    private int fpsCount = 0;
    private long fpsStartTime = System.currentTimeMillis();

    public BoidSimulationScreen() {
        // Inicializa 500 boids em posições aleatórias
        boids = new Boid[500]; 
        for (int i = 0; i < boids.length; i++) {
            boids[i] = new Boid((float)(Math.random() * 800), (float)(Math.random() * 600));
        }
        
        renderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        batch = new SpriteBatch();
        lastFrameTime = 0;
    }
    
    private void updateBoidsParallel() {
        int numThreads = 4;
        Thread[] threads = new Thread[numThreads];
        int chunkSize = boids.length / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = (t == numThreads - 1) ? boids.length : (t + 1) * chunkSize;

            threads[t] = new Thread(() -> {
                for (int i = start; i < end; i++) {
                    boids[i].update(boids);
                }
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void render(float delta) {
        // Alterna o modo quando a tecla ESPAÇO é pressionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            useParallel = !useParallel;
        }

        long frameStartTime = System.currentTimeMillis();

        // Atualiza os boids no modo selecionado
        if (useParallel) {
            updateBoidsParallel();
        } else {
            for (Boid boid : boids) {
                boid.update(boids);
            }
        }

        // Limpa a tela e renderiza os boids
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Boid boid : boids) {
            renderer.circle(boid.position.x, boid.position.y, 2);
        }
        renderer.end();
        
        // Calcula o tempo do frame
        lastFrameTime = System.currentTimeMillis() - frameStartTime;
        float frameTimeSeconds = lastFrameTime / 1000f; // converte para segundos

        // Cálculo do FPS mais preciso
        fpsCount++;
        if (System.currentTimeMillis() - fpsStartTime >= 1000) {
            averageFps = fpsCount;
            fpsCount = 0;
            fpsStartTime = System.currentTimeMillis();
            
            // Exibe FPS no console do Eclipse
            System.out.println("FPS: " + averageFps);
            Gdx.app.log("FPS", "FPS: " + averageFps);
        }

        // Renderiza as informações na tela
        batch.begin();
        font.draw(batch, "Modo: " + (useParallel ? "Paralelo" : "Sequencial"), 10, 590);
        font.draw(batch, String.format("Tempo por frame: %.4f s", frameTimeSeconds), 10, 570);
        font.draw(batch, "FPS: " + averageFps, 10, 550);  // Exibe FPS médio na tela
        font.draw(batch, "Boids: " + boids.length, 10, 530);
        font.draw(batch, "Pressione ESPAÇO para alternar", 10, 510);
        batch.end();
    }
    
    @Override
    public void dispose() {
        // Libera todos os recursos
        renderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
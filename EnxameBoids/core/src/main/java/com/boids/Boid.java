package com.boids;

import com.badlogic.gdx.math.Vector2;

public class Boid {
    public Vector2 position;
    public Vector2 velocity;

    public Boid(float x, float y) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2((float)Math.random()*2-1, (float)Math.random()*2-1).nor();
    }

    public void update(Boid[] allBoids) {
        // Regras do Boid (simplificadas)
        Vector2 alignment = new Vector2();
        Vector2 cohesion = new Vector2();
        Vector2 separation = new Vector2();

        int count = 0;
        for (Boid other : allBoids) {
            if (other == this) continue;

            float distance = this.position.dst(other.position);
            if (distance < 50) {
                alignment.add(other.velocity);
                cohesion.add(other.position);
                separation.add(new Vector2(this.position).sub(other.position).scl(1 / distance));
                count++;
            }
        }

        if (count > 0) {
            alignment.scl(1f / count).nor();
            cohesion.scl(1f / count).sub(this.position).nor();
            separation.scl(1f / count).nor();
        }

        // Combina forças
        Vector2 acceleration = new Vector2();
        acceleration.add(alignment).add(cohesion).add(separation);
        velocity.add(acceleration).limit(2);
        position.add(velocity);
        
     // Largura e altura da tela

        float screenWidth = 1920;
        float screenHeight = 1080;
        
        if (position.x < 0) position.x = screenWidth;
        if (position.x > screenWidth) position.x = 0;
        if (position.y < 0) position.y = screenHeight;
        if (position.y > screenHeight) position.y = 0;
    }
}
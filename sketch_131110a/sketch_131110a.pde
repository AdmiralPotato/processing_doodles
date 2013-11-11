/**
 * Created with IntelliJ IDEA.
 * User: admiral
 * Date: 11/11/13
 * Time: 3:40 AM
 * A sketch designed to start, export some PNGs, then exit
 */

import java.lang.Math;
import java.util.*;
import processing.core.*;

public class sketch_131110a extends PApplet {
	ArrayList<Particle> particleList = new ArrayList<Particle>();
	int
			//change outputScale to 2 for final export, then use an image editor to scale down for better antialiasing
			outputScale = 2,
			windowSize = 480 * outputScale,
			half = (int) windowSize / 2,
			maxFrames = 60,
			frameNum = 0;

	double
			pi = Math.PI,
			tau = pi * 2,
			deg = pi / 180;

	double sinFrac(double r){
		return (double) (Math.sin(r) / 2) + 0.5;
	}
	double cosFrac(double r){
		return (double) (Math.sin(r) / 2) + 0.5;
	}

	//Set to fullscreen if app has problems launching - this seems to increase rate of successful sketch launch;
	//On macs, it seems to have problems initializing the rendering context larger than 512px
	public boolean sketchFullScreen() {
		return false;
	}

	public void setup() {
		size(windowSize, windowSize, P2D);
		frameRate(120);
		addParticle(36, 0, 6, 11);
		addParticle(36, (double)(1 / 40), 6, 7);
		addParticle(36, (double)(1 / 80), 6, 3);
	}

	public void draw() {
		int i, len = particleList.size();
		Particle particle;
		background(0x000000);
		//clear();
		colorMode(HSB, 1, 1, 1, 1);
		translate(half, half);
		scale(outputScale);
		smooth(8);
		ellipseMode(RADIUS);  // Set ellipseMode to RADIUS
		noFill();
		stroke(0, 1, 1);
		blendMode(SCREEN);

		//update loop traverses backwards in case anything dies
		for(i = len - 1; i >= 0; i--){
			particle = particleList.get(i);
			particle.update();
		}

		//render loop traverses forward for painter's algorithm
		for(i = 0; i < len; i++){
			particle = particleList.get(i);
			particle.render();
		}
		saveFrame("1/###.png");
		frameNum++;
		if(frameNum >= maxFrames){
			noLoop();
			exit();
		}
	}

	void addParticle(int n, double phase, double curl, double speed) {
		int i;
		double frac = 1 / ((double) n);
		println("frac:", frac);
		for(i = 0; i < n; i++){
			particleList.add(
					new Particle(
							i, //offset
							frac, //angle
							phase, //phase
							0, //x
							0, //y
							curl, //curl
							speed //speed
					)
			);
		}
	}

	class Particle {
		int
				offset;
		double
				x, y,
				velX, velY,
				frac, phase,
				angle, speed, curl,
				circleRadius = 10;
		Particle (int o, double f, double p, double iY, double iX, double c, double s) {
			offset = o;
			frac = f;
			phase = p;
			angle = ((offset * frac) + phase) * tau;
			//println(offset, frac, angle);

			x = iX;
			y = iY;
			speed = s;
			curl = c * deg;
		}
		void rot() {
			velX = Math.cos(angle) * speed;
			velY = Math.sin(angle) * speed;
		}
		void wrap() {
			//if it's offscreen, put it back on in the middle
			if(Math.abs(x) > half || Math.abs(y) > half){
				x = (Math.abs(x) - half) * Math.signum(x);
				y = (Math.abs(y) - half) * Math.signum(y);
			}
		}
		void update() {
			angle += curl;
			rot();
			x += velX;
			y += velY;
			wrap();
		}
		void render() {
			stroke(
					(float) ((sinFrac(angle) * 0.25) + 0.5) % 1, //hue
					1.0f, //saturation
					0.75f, //luminosity
					1 //alpha
			);
			strokeWeight(2);
			strokeCap(ROUND);
			strokeJoin(MITER);

			line(
					(float) x,
					(float) y,
					(float) (x - (velX * circleRadius * 0.5)),
					(float) (y - (velY * circleRadius * 0.5))
			);

			ellipse(
					(float) x,
					(float) y,
					(float) circleRadius,
					(float) circleRadius
			);
		}
	}

}
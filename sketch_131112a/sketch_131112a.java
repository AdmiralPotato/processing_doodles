/**
 * Created with IntelliJ IDEA.
 * User: admiral
 * Date: 11/12/13
 * Time: 12:01 AM
 * Gonna have some things on a grid!
 */

import java.lang.Math;
import processing.core.*;
import java.util.*;

public class sketch_131112a extends PApplet {
	ArrayList<Ob> obList = new ArrayList<Ob>();
	int
			//change outputScale to 2 for final export, then use an image editor to scale down for better antialiasing
			outputScale = 1,
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
		int
			i, j,
			n = 6,
			spacing = windowSize / n;

		size(windowSize, windowSize, P2D);
		frameRate(24);
		obList.add(new Grid(6));

		for(i = 0; i < n; i++){
			for(j = 1; j < n; j++){
				obList.add(new Target(-half + (spacing * i), -half + (spacing * j), i % 2, j * i % 2));
			}
		}

		addParticle(10, 0, 6, 4);
	}

	public void draw() {
		int i, len = obList.size();
		Ob ob;
		background(0x000000);
		//clear();
		colorMode(HSB, 1, 1, 1, 1);
		translate(half, half);
		scale(outputScale);
		smooth(8);
		ellipseMode(RADIUS);	// Set ellipseMode to RADIUS
		noFill();
		stroke(0, 1, 1);
		blendMode(SCREEN);

		//update loop traverses backwards in case anything dies
		for(i = len - 1; i >= 0; i--){
			ob = obList.get(i);
			ob.update();
		}

		//render loop traverses forward for painter's algorithm
		for(i = 0; i < len; i++){
			ob = obList.get(i);
			ob.render();
		}

		//saveFrame("0/###.png");
		//frameNum++;
		//if(frameNum >= maxFrames){
		//	noLoop();
		//	exit();
		//}
	}

	public abstract class Ob{
		public void update(){}
		public void render(){}
	}

	public class Grid extends Ob{
		ArrayList<Integer> lineList;
		public Grid(int n){
			int
				i,
				spacing = windowSize / n;
			lineList = new ArrayList<Integer>();
			for(i = 1; i < n; i++){
				lineList.add(i * spacing);
			}
		}
		public void update(){

		}
		public void render(){
			int i, len = lineList.size(), n;
			stroke(0, 0, 0.4f, 0);
			for(i = 0; i < len; i++){
				n = lineList.get(i);
				line(n -half, -half, n -half, half);
				line(-half, n -half, half, n -half);
			}
		}
	}

	public class Target extends Ob{
		int
			x, y,
			velX, velY,
			phase,
			targetOuter = 5, targetInner = 2;
		float tO, tI;

		public Target(int aX, int aY, int vX, int vY){
			x = aX;
			y = aY;
			velX = vX;
			velY = vY;
			phase = 0;
		}
		public void update(){
			x += velX;
			y += velY;
			if(x > half){
				x = -half;
			}
			if(y > half){
				y = -half;
			}
			phase += 36;
			tO = (float) (sinFrac(deg * phase) * 2) + 1;
			tI = (float) (sinFrac(deg * phase) * 1) + 2;
		}
		public void drawTargetShape(){
			stroke(0.2f, 1, 1, 0);

			line(-targetOuter * tO, -targetOuter * tO, -targetInner * tI, -targetOuter * tO);
			line(targetInner * tI, -targetOuter * tO, targetOuter * tO, -targetOuter * tO);
			line(targetOuter * tO, -targetOuter * tO, targetOuter * tO, -targetInner * tI);
			line(targetOuter * tO, targetInner * tI, targetOuter * tO, targetOuter * tO);
			line(targetOuter * tO, targetOuter * tO, targetInner * tI, targetOuter * tO);
			line(-targetInner * tI, targetOuter * tO, -targetOuter * tO, targetOuter * tO);
			line(-targetOuter * tO, targetOuter * tO, -targetOuter * tO, targetInner * tI);
			line(-targetOuter * tO, -targetInner * tI, -targetOuter * tO, -targetOuter * tO);

		}
		public void render(){
			pushMatrix();
			translate(x, y);
			//scale((float) (1 + sinFrac(deg * phase)));
			drawTargetShape();
			popMatrix();
		}
	}


	void addParticle(int n, double phase, double curl, double speed) {
		int i;
		double frac = 1 / ((double) n);
		println("frac:", frac);
		for(i = 0; i < n; i++){
			obList.add(
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

	class Particle extends Ob{
		int
				offset;
		double
				x, y,
				velX, velY,
				frac, phase,
				angle, speed, curl,
				circleRadius = 10;
		public Particle (int o, double f, double p, double iY, double iX, double c, double s) {
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
		public void rot() {
			velX = Math.cos(angle) * speed;
			velY = Math.sin(angle) * speed;
		}
		public void wrap() {
			//if it's offscreen, put it back on in the middle
			if(Math.abs(x) > half || Math.abs(y) > half){
				x = (Math.abs(x) - half) * Math.signum(x);
				y = (Math.abs(y) - half) * Math.signum(y);
			}
		}
		public void update() {
			angle += curl;
			rot();
			x += velX;
			y += velY;
			wrap();
		}
		public void render() {
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

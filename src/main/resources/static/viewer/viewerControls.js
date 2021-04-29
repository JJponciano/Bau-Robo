
/**
 * @author Rebekka Lange
 */

// find the latest release here https://unpkg.com/three
import * as THREE from 'https://unpkg.com/three@0.125.1/build/three.module.js';
import { TrackballControls } from 'https://unpkg.com/three@0.125.1/examples/jsm/controls/TrackballControls.js';
import { Color } from 'https://unpkg.com/three@0.125.1/src/math/Color.js';
import { Box3 } from 'https://unpkg.com/three@0.125.1/src/math/Box3.js';
import { Vector3 } from 'https://unpkg.com/three@0.125.1/src/math/Vector3.js';
import { AxesHelper } from 'https://unpkg.com/three@0.125.1/src/helpers/AxesHelper.js';
import { Earcut } from 'https://unpkg.com/three@0.125.1/src/extras/Earcut.js';

/**************************************************scene variables*****************************************************/

/*
 * Create a scene
 */
var scene = new THREE.Scene();

/*
 *  Create a camera
 */
var camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);

/*
 *  Create a renderer and bind it to canvas
 */
var renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setClearColor(new THREE.Color(0xEEEEEE));
var canvas = document.getElementById('myCanvas')
var clWidth = canvas.clientWidth;
var clHeight = canvas.clientHeight;
renderer.setSize(clWidth, clHeight, false);
document.getElementById('myCanvas').appendChild(renderer.domElement);

/*
 *  Create a raycaster to get the objects clicked
 *  set point threshold to catch a point, if clicked in a radius around it
 */
var raycaster = new THREE.Raycaster();
raycaster.params.Points.threshold = 0.1;
var mouse = new THREE.Vector2();

/*
 *  Show the x-,y- and z-axis for th user-orientation
 *  x: red, y: green, z: blue
 */
var axesHelper = new THREE.AxesHelper( 10 );
scene.add( axesHelper );

/*
 *	Add an event listener to handle user-action
 */
document.addEventListener('dblclick', click, false);

/*
 *	Add controls to move through the scene
 */
var controls = new TrackballControls(camera, renderer.domElement);

/****************************************information storage***********************************************************/


/*
 * Define colors for objects classes
 */
var Wand = new Color('khaki');
var Boden = new Color('gray');
var Tuer = new Color('brown');
var Fenster = new Color('skyblue');
var Decke = new Color('lightgreen');
var Steckdose = new Color('black');
var Undefiniert = new Color('violet');

/*
 *  Arrays to hold the objects and points clickable for the user
 */
var clickable = [];
var clickablePoints = [];

/*
 *  Pointcloud to click on for polyon creation
 */
var pointcloud = [];

/*
 *  Variables to store the current marked object
 */
export var _clicked;
export var _clickedPoint;

/*
 *  The points the user sets by new polygon creation to show which he set
 */
var show_points = [];

/*
 *  The lines between the points to show the edges
 */
var show_lines;

/*
 *  The points the user sets by new polygon creation to create the new polygon
 */
var points = [];

/*
 *  The URL to backend
 */
var url = 'http://localhost:8080';

/*
 *  Store changes in frontend
 *  stores an array for each action: type, id, new attribute, old attribute
 */
var log = [];

/*
 *  Selection mode
 */
export var selectMode = 'Objekt';

/*
 *  Status
 */
export var status = 'Start';

/*
 *
 */
var newObjClass = 'Undefiniert';

/*
 *
 */
var objInfo = 'Wähle ein Objekt...';

/**************************************load initial scene**************************************************************/

/*
 *  Call the functions to load all objects and the point cloud of the selected folder
 */
loadObjects();
loadPointcloud();

/*
 *  Render the scene
 */
render();

/***********************************load objects and pointcloud********************************************************/

/*
 *	Call GET-method to retrieve all objects to be loaded
 *  Calls loadObject(json) to load every object given by GET-call
 *  Calls lookAtScene() to center the camera on all objects after loading
 */
async function loadObjects(){

	// get data from backend
	await fetch(url + '/allObjects', { method: 'GET'})
	// get response as json
	.then(response => response.json())
	// response in data
	.then(data => {

        //  load every object in data
		for(let k = 0; k < data.length; k++){
			loadObject(data[k]);
		}

		// set camera on loaded objects
		lookAtScene();

        // update status
		status = 'Objekte geladen';

	})
	.catch(error => {

		// update status
		status = 'Fehler beim Laden der Objekte';

	});
 }

/*
 *	Loads an object from a json and adds it to the scene and clickable-objects list
 *  @param  json containing the object information
 */
function loadObject(json){

	// create an array with x-,y- and z-coordinates for each vertex (x1,y1,z1,x2,y2,z2,...)
	let vertices = [];
	for(let i = 0; i < json.x.length; i++){
		vertices.push(json.x[i], json.y[i], json.z[i]);
	}

	// create array to store the vertex indices
	// indices can only be stored for triangles
	// if not given, a polygon of more than 3 vertices needs to be cut in triangles
	let indices = [];
	for(let j = 0; j < json.vertex_indices.length; j++){
		//TODO: Flächen dürfen momentan nur aus drei oder vier Punkten bestehen
		if(json.vertex_indices[j].length == 4){
			indices.push(json.vertex_indices[j][0], json.vertex_indices[j][1], json.vertex_indices[j][2]);
			indices.push(json.vertex_indices[j][2], json.vertex_indices[j][3], json.vertex_indices[j][0]);
		}
		else if(json.vertex_indices[j].length == 3){
			indices.push(json.vertex_indices[j][0], json.vertex_indices[j][1], json.vertex_indices[j][2]);
		}
	}

	// create a geometry and add the vertices and vertex indices
	let geometry = new THREE.BufferGeometry();
	geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
	geometry.setIndex(indices);

	// create a material with the set color
	let material = new THREE.MeshBasicMaterial({color: Undefiniert, side: THREE.DoubleSide, transparent: true, opacity: 0.5});

	// create the object with material and geometry
	let object = new THREE.Mesh( geometry, material );

	// render only in visible
	object.frustumCulled = true;

	// save object id
	object.name = json.objID;

	// save object class
	let userData = [];
	userData.push(json.objClass);
	object.userData = userData;

    // update color
    updateColor(object);

	// add object to clickable list
	clickable.push(object);

	// add object to scene
	scene.add(object);

}

/*
 * Calls GET-method to retrieve a pointcloud
 * Loads all given points
 */
async function loadPointcloud(){

    //  Get the pointcloud from backend
	await fetch(url + '/getPointcloud', { method: 'GET'})
	//  get response as json
	.then(response => response.json())
	// get response in data
	.then(data => {

	    // store the vertex coordinates for each vertex in an array (x1,y1,z1,x2,y2,z3,...)
		let vertices = [];
		for(let i = 0; i < data.x.length; i++){
			vertices.push(data.x[i], data.y[i], data.z[i]);
		}

		// create a geometry containing the vertices
		let geometry = new THREE.BufferGeometry().setAttribute( 'position', new THREE.Float32BufferAttribute( vertices, 3 ) );

		// create a material
		// size describes the points size
		// sizeAttenuation=false disables size changes when zooming
		let material = new THREE.PointsMaterial( { color: 0x888888, size: 1, sizeAttenuation: false } );

		// create points from geometry and material
		pointcloud = new  THREE.Points( geometry, material );

		// add the points to the scene
		scene.add(pointcloud);

        // update status
		status = 'Punktwolke geladen';

	})
	.catch(error => {

		// update status
		status = 'Fehler beim Laden der Punktwolke';

	});
}

/*************************************save in backend******************************************************************/

/*
 *
 */
export async function makePersistent(){

    // count errors
    let errorCount = 0;

    // send every change to backend
    for(let i = 0; i < log.length; i++){

        // get the changed object
        let object = scene.getObjectByName(log[i][1]);

        // create a json from the changed object
        let json = createJSON(object);

        // send changed object to backend
        await fetch(url + '/changeObject', { method: 'PUT', headers: {'Content-Type': 'application/json'},body: JSON.stringify(json)})
        .then(response => response.json())
        .then(data => {

            // check for error
            if(!data){

                // increment success count
                errorCount ++;

                // undo changes in frontend
                // get type of change
                switch(log[i][0]){

                    case 'newClass':

                        // set old class
                        object.userData[0] = log[i][2];

                        // reset color
                        updateColor(object);

                        break;

                    case 'translationObjekt':

                        // get number of points
                        let count = object.geometry.getAttribute('position').count;

                        // for every point
                        // every vertex needs to be changed to apply translation on bufferGeometry
                        for(let j = 0; j < count; j++){

                            // translate
                            translateObject(object, j, -1*parseInt(log[i][3]), log[i][2]);

                        }

                        // update geometry
                        object.geometry.attributes.position.needsUpdate = true;

                        // check if object was selected
                        if(_clicked && _clicked.name == object.name){

                            // reset points
                            removePoints(clickablePoints);
                            clickablePoints = [];
                            _clickedPoint = null;
                            loadClickablePoints(_clicked);

                        }

                        break;

                    case 'translationPunkt':

                        // move object back
                        translateObject(object, parseInt(log[i][2]), -1*log[i][4], log[i][3]);

                        // update geometry
                        object.geometry.attributes.position.needsUpdate = true;

                        // check if object was selected
                        if(_clicked && _clicked.name == object.name){

                            // reset points
                            removePoints(clickablePoints);
                            clickablePoints = [];
                            _clickedPoint = null;
                            loadClickablePoints(_clicked);

                        }

                        break;

                    case 'rotation':

                        // move object back
                        rotation(object, log[i][2], -1*log[i][3]);

                        break;

                }

            }

		})
		.catch(error => {

			// update status
            status = 'Fehler bei Speichervorgang';

		});

    }

    // clear log
    log = [];

    // update status
    status = 'Speichern mit ' + errorCount.toString() + ' Fehlern';

    // update scene
    render();

}

/****************************************object selection**************************************************************/

/*
 *	Checks which editing mode is selected: new polygon, object selection, point selection
 *  new polygon -> sets a point at selected coordinates and store the coordinates
 *  object selection -> highlight selected object and load objects vertices in scene
 *  point selection -> highlight selected point
 *  @param event
 */
function click(event){

	// get the mouse coordinates
	let rect = event.target.getBoundingClientRect();
	mouse.x = ((event.clientX - rect.left) / clWidth) * 2 - 1;
	mouse.y = - ((event.clientY - rect.top) / clHeight) * 2 + 1;

	// set raycaster
	raycaster.setFromCamera(mouse, camera);

	// check if a new polygon shall be created
	if(selectMode == 'Neu'){

	    newPolygon();

        // update status
        status = 'Setze neues Objekt';
	}

	// check if an object shall be selected
	else if(selectMode == 'Objekt'){

        objectSelection();

        // update status
        status = 'Objekt ausgewählt';

	}

	// check if object selection is set to point
	else if(selectMode == 'Punkt'){

        pointSelection();

        // update status
        status = 'Punkt ausgewählt';

	}

	// update scene
	render()
}

/*
 *
 */
function newPolygon(){

    // get mouse intersection with pointcloud
    //ToDO intersection with pointcloud
    //let intersects = raycaster.intersectObject( pointcloud );
    let intersects = raycaster.intersectObjects( clickable );

    // if the user clicked on a point
    if ( intersects.length > 0 ) {

        // create a sphere at the clicked coordinates to show the user the point
        var point = new THREE.Mesh( new THREE.SphereGeometry(0.05, 10, 10), new THREE.MeshBasicMaterial( { color: 'red' } ) );
        point.position.copy(intersects[0].point);
        scene.add(point);

        // add the sphere to show points array to be able to remove it when not needed anymore
        show_points.push(point);

        // remove the last point which is also the first point
        if(points.length > 1){
            points.splice(-3, 3);
        }

        // add the coordinates to the point array to store it for the final object creation
        points.push(point.position.x, point.position.y, point.position.z);

        // add the first point also as last one
        points.push(points[0], points[1], points[2]);

        // update lines
        removeLines(show_lines);
        loadLines(points);

        // update status
        status = 'Punkt ' + points.length + ' gesetzt';

    }

}

/*
 *
 */
function objectSelection(){

    // check intersection with clickable objects
    let intersects = raycaster.intersectObjects(clickable);

    // if the user clicked on a object
    if (intersects.length > 0) {

        // check if an object is currently selected and if its the same as the new one
        if(_clicked){

            // remove the clickable points from the last selected object
            removePoints(clickablePoints);
            clickablePoints = [];
            _clickedPoint = null;

            // remove the highlighter from the last selected object
            removeHighlighter(_clicked);
            _clicked = null;

        }

        //  check if no object is currently selected


        // highlight newly selected object
        _clicked = intersects[0].object;
        setHighlighter(_clicked);



        // update current objects information in gui
        objInfo = 'ID: ' + _clicked.name.toString() + ', Klasse: ' + _clicked.userData[0].toString();

        // load objects points to show the user
        loadClickablePoints(_clicked);

        // update status
        status = 'Objekt gewählt';

    }
    else{

        if(_clicked){

            // remove the clickable points from the last selected object
            removePoints(clickablePoints);
            clickablePoints = [];
            _clickedPoint = null;

            // remove highlighter
            removeHighlighter(_clicked);
            _clicked = null;

        }

    }
}

/*
 *
 */
function pointSelection(){

    // check intersection with clickable points
    let intersects = raycaster.intersectObjects(clickablePoints);

    // check if the user clicked on a point
    if (intersects.length > 0) {

        // check if a point is currently selected which is not the new one
        if(_clickedPoint){

            // remove highlighter on old selection
            removeHighlighter(_clickedPoint);
            _clickedPoint = null;

        }

        // check if no point is currently selected

        // store the first intersected object
        _clickedPoint = intersects[0].object;

        // highlight selected object
        setHighlighter(_clickedPoint);


        // update status
        status = 'Punkt gewählt';

        // update info
        objInfo = 'Objekt-ID: ' + _clicked.name.toString() + ' , Klasse: ' + _clicked.userData[0].toString();

    }
}

/*******************************************change color***************************************************************/

/*
 *
 */
function updateColor(object){

    // get color of class
	let color;
	let newClass = object.userData[0];
	if(newClass == 'Wall' || newClass == 'wall' || newClass == 'Wand'){
		color = Wand;
	}
	else if(newClass == 'Ceiling' || newClass == 'ceiling' || newClass == 'Decke'){
		color = Decke;
	}
	else if(newClass == 'Ground' || newClass == 'ground' || newClass == 'Boden'){
		color = Boden;
	}
	else if(newClass == 'Window' || newClass == 'window' || newClass == 'Fenster'){
		color = Fenster;
	}
	else if(newClass == 'Socket' || newClass == 'socket' || newClass == 'Steckdose'){
		color = Steckdose;
	}
	else if(newClass == 'Door' || newClass == 'door' || newClass == 'Tuer'){
		color = Tuer;
	}
	else{
		color = Undefiniert;
	}

	// set the color
	object.material.color = color;

	// update scene
	render();

}

/**************************************change object class*************************************************************/

/*
 *  Retrieves the newly selected class for a selected object
 *  Calls PUT-method to send the changes to backend
 *  If the changes succeeded in backend, change the color of the object according to its new class
 *  If not, leave the old class
 */
export function changeClass(newClass){

    // check if an object is selected and object selection is set to object
	if(_clicked && selectMode == 'Objekt'){

		// save the new class in object
		_clicked.userData[0] = newClass;

		// update color
		updateColor(_clicked);

        // update status
        status = 'Klasse geändert';

        // update info
        objInfo = 'ID: ' + _clicked.name.toString() + ', Klasse: ' + _clicked.userData[0].toString();

        // update scene
        render();
	}
}

/*
 *
 */
export function setNewObjClass(newClass){

    // set class for new object
    newObjClass = newClass;

}

/************************************save new polygon******************************************************************/

/*
 *	Creates a new object from the set points
 *  Calls POST-method to send new object to backend
 *  Adds object to scene if successful, else not
 */
export async function saveNewObject(){

	// check if at least three points were set
	if(points.length >= 12){

		// create geometry from vertices
		let geometry = new THREE.BufferGeometry();
		geometry.setAttribute('position', new THREE.Float32BufferAttribute(points, 3));

        // remove last point -> duplicate of first point
        points.splice(-3,3);

        // get indices
        let indices = getVertexIndices(points);
		geometry.setIndex( indices );

        // create material
		let material = new THREE.MeshBasicMaterial({color: Undefiniert, side: THREE.DoubleSide, transparent: true, opacity: 0.5});

		// create object
		let object = new THREE.Mesh( geometry, material );

		// save id, class
		object.name = 0;
		object.userData[0] = newObjClass;

		// get object as json
		let json = createJSON(object);

        // send new object to backend
		await fetch(url + '/newObject', { method: 'POST', headers: {'Content-Type': 'application/json'},body: JSON.stringify(json)})
		.then(response => response.text())
		.then(data => {

            // data contains the new id or "" if not created
            // if the creation succeeded in backend
			if(data != ""){


				// add object to scene and clickable objects
				clickable.push(object);
				scene.add(object);

				// save objects id
				object.name = data;

				// store new object in selected one highlight selected object
				_clicked = object;
				setHighlighter(_clicked);

				// update display
				objInfo = 'ID: ' + _clicked.name.toString() + ', Klasse: ' + _clicked.userData[0].toString();

				// set color
				updateColor(_clicked);

				// load points to show objects vertices
				loadClickablePoints(_clicked);

				// update status
				status = 'Neues Objekt erstellt';

			}
		})
		.catch(error => {

		    // update status
		    status = 'Fehler beim Speichern des Objektes';

		});

	}

	// reset points array for next polygon creation
	points = [];

	// remove show points from scene
	removePoints(show_points);
    show_points = [];

	// remove lines from scene
	removeLines(show_lines);
    show_lines = [];

    // update scene
	render();
}

/*
 *
 */
function getVertexIndices(vertices){

        // earcut algorithm only considers x and y coords
        // find the plane with the lowest differences
        // set the other two as x and y and generate indices
        let minX = vertices[0];
        let maxX = vertices[0];
        let minY = vertices[1];
        let maxY = vertices[1];
        let minZ = vertices[2];
        let maxZ = vertices[2];
        let triPoints = [];
        for(let i = 1; i < vertices.length/3; i++){
            if(vertices[(3*i)] > maxX){
                maxX=vertices[(3*i)];
            }
            else if(vertices[(3*i)] < minX){
                minX=vertices[(3*i)];
            }
            if(vertices[(3*i)+1] > maxY){
                maxY=vertices[(3*i)+1];
            }
            else if(vertices[(3*i)+1] < minY){
                minY=vertices[(3*i)+1];
            }
            if(vertices[(3*i)+2] > maxZ){
                maxZ=vertices[(3*i)+2];
            }
            else if(vertices[(3*i)+2] < minZ){
                minZ=vertices[(3*i)+2];
            }
        }
        let diffX = maxX - minX;
        let diffY = maxY - minY;
        let diffZ = maxZ - minZ;
        if(diffX <= diffY && diffX <= diffZ){
            for(let i = 0; i < vertices.length/3; i++){
                triPoints.push(vertices[(3*i)+1], vertices[(3*i)+2]);
            }
        }
        else if(diffY <= diffX && diffY <= diffZ){
            for(let i = 0; i < vertices.length/3; i++){
                triPoints.push(vertices[(3*i)], vertices[(3*i)+2]);
            }
        }
        else if(diffZ <= diffX && diffZ <= diffY){
            for(let i = 0; i < vertices.length/3; i++){
                triPoints.push(vertices[(3*i)], vertices[(3*i)+1]);
            }
        }

		// create triangles to get vertex indices
		let indices = Earcut.triangulate(triPoints, null, 2);

        return indices;
}

/*
 *
 */
export function cancelNewObject(){

    // remove set points
    removePoints(show_points);
    show_points = [];

    // remove lines
    removeLines(show_lines);
    show_lines = [];

    // clear points
    points = [];

    // update status
    status = 'Objekterstellung abgebrochen';

    // update info
    objInfo = 'Wähle Objekt...';

    // update scene
    render();

}

/**************************************delete object*******************************************************************/

/*
 *	Calls DELETE-method to delete a selected object in backend
 *  If succeeded in backend, remove object from scene and clickable list
 */
export async function deleteObject(){


    // check if an object is selected
	if(_clicked){

        // check if the object selection is set to object
		if(selectMode == 'Objekt'){

			// send id of object to backend
			await fetch(url + '/delete', { method: 'DELETE', body: _clicked.name })
			.then(response => response.json())
			.then(data => {

			    // check if the deletion succeeded in backend
				if(data){

					// remove object from clickable list
					for(let i = 0; i < clickable.length; i++){
						if(clickable[i].name == _clicked.name){
							clickable.splice(i, 1);
						}
					}

					// remove object from scene
					removeFromScene(_clicked);

					// remove highlighter
					removePoints(clickablePoints);
                    clickablePoints = [];
					_clicked = null;

					//  update status
					status = 'Objekt gelöscht';

					// update scene
					render();
				}
			})
			.catch(error => {

				// update status
				status = 'Fehler beim Löschen';

			});
		}
	}

    // update scene
    render();
}

/**************************************geometry manipulation***********************************************************/

/*
 *	Rotates a selected object around a given axis in a given angle
 *  Calls rotateObject(object, angle, axis) to rotate object
 *  Updates geometry and clickable points
 *  Sends object changes to backend
 *  If succeeded in backend, changes stay
 *  If not, changes get undone
 *  @params axis to rotate around, angle of rotation
 */
export function rotation(object, axis, angle){

    // check if an object is selected and the object selection is set to object
	if(selectMode == 'Objekt'){

		// apply rotation to objects geometry
		rotateObject(object, (angle*Math.PI/180), axis);

		// update geometry
		object.geometry.attributes.position.needsUpdate = true;

		// reload clickable points
		removePoints(clickablePoints);
        clickablePoints = [];
		loadClickablePoints(object);

        // update scene
        render();

	}
}

/*
 *	Translates a given object to (0,0,0), rotates around given axis in given angle and translates back
 *  @params object to be rotated, angle in radians, axis to rotate around
 */
function rotateObject(object, rad, axis){

    // get the center of the object to translate to (0,0,0)
    object.geometry.computeBoundingSphere();
    let center = new Vector3().copy(object.geometry.boundingSphere.center);

    // get the number of points
	let count = object.geometry.getAttribute('position').count;

	// for every point
	// translation needs to be applied on every vertex, rotation can be done on complete geometry
	for(let i = 0; i < count; i++){

	    // translate with center vector to move whole object to (0,0,0)
		object.geometry.getAttribute('position').setXYZ(i, object.geometry.getAttribute('position').getX(i)-center.x, object.geometry.getAttribute('position').getY(i)-center.y, object.geometry.getAttribute('position').getZ(i)-center.z);
	}

	// check which axis is selected
	switch(axis){

		case 'x':

		    // rotate object around x
			object.geometry.rotateX(rad);
			break;

		case 'y':

		    // rotate object around y
			object.geometry.rotateY(rad);
			break;

		case 'z':

		    // rotate object around z
			object.geometry.rotateZ(rad);
			break;
	}

	// for every point
	for(let i = 0; i < count; i++){

	    // translate back with center vector to move object to initial position but with rotation
		object.geometry.getAttribute('position').setXYZ(i, object.geometry.getAttribute('position').getX(i)+center.x, object.geometry.getAttribute('position').getY(i)+center.y, object.geometry.getAttribute('position').getZ(i)+center.z);
	}

}

/*
 *	Translates an object or point on a given axis in a given step size
 *  Calls PUT-method to send changes to backend
 *  If an error occurs, undo changes in frontend
 *  @params axis to translate on, step size
 */
export function translation(object, axis, step){

    // check if object selection is set to object
    if(selectMode == 'Objekt'){

        // get number of points
        let count = object.geometry.getAttribute('position').count;

        // for every point
        // every vertex needs to be changed to apply translation on bufferGeometry
        for(let i = 0; i < count; i++){

            // translate
            translateObject(object, i, step, axis);
        }
    }

    // check if object selection is set to point
    else if(selectMode == 'Punkt' && _clickedPoint){

        // translate point
        // the point name is the index of the vertex in objects geometry
        translateObject(object, parseInt(_clickedPoint.name), step, axis);
    }

    // update geometry
    object.geometry.attributes.position.needsUpdate = true;

    // reload clickable points
    removePoints(clickablePoints);
    clickablePoints = [];
    loadClickablePoints(object);

    // update scene
    render();

}

/*
 *  Translates an object at given position in an array on given axis in given step size
 *  @params array which holds objects, index to find object to be translated, step size, axis to translate on
 */
function translateObject(object, index, step, axis){

    // check which axis is selected
	switch(axis){

		case 'x':

		    // translate on x
			object.geometry.getAttribute('position').setX(index, object.geometry.getAttribute('position').getX(index)+step);
			break;

		case 'y':

		    // translate on y
			object.geometry.getAttribute('position').setY(index, object.geometry.getAttribute('position').getY(index)+step);
			break;

		case 'z':

		    // translate on z
			object.geometry.getAttribute('position').setZ(index, object.geometry.getAttribute('position').getZ(index)+step);
			break;
	}
}

/***************************************undo last step*****************************************************************/

/*
 *	Calls POST-method to get the last changes from backend
 *  Undoes the last action
 */
export async function undo(){

	// else get last action from backend
    await fetch(url + '/undo', { method: 'POST'})
    .then(response => response.text())
    .then(data => {

        if(data.length > 2){
            // string contains the action and the object -> action(object-as-json)
            // split the string to get the called function and the object separately
            // function at split[0], object at split[1]
            let split = data.split(/[()]/);

            // parse object string to json
            let json = JSON.parse(split[1]);

            // check which function was called last
            switch(split[0]){

                // a new object was created
                // in this case the json only contains the objects id
                case 'undo_newObject':

                    // check if the currently selected object was the new one
                    if(_clicked && _clicked.name == split[1]){

                        // set selected object to null
                        _clicked = null;

                        // remove points and selected point
                        removePoints(clickablePoints);
                        clickablePoints = [];
                        _clickedPoint = null;

                    }

                    // get the new object
                    scene.children.forEach(function(object){

                        // check for the same id
                        if(object.name == split[1]){

                            // remove from scene
                            scene.remove(object);
                            object.geometry.dispose();

                            // remove object from clickable list
                            for(let i = 0; i < clickable.length; i++){
                                if(clickable[i].name == object.name){
                                    clickable.splice(i, 1);
                                    break;
                                }
                            }
                        }
                    });

                    break;

                // if a change needs to be undone
                case 'undo_changeObject':

                    // get the object
                    scene.children.forEach(function(object){

                        // check for the same id
                        if(object.name == json.objID){

                            //
                            if(_clicked && _clicked.name == json.objID){
                                removePoints(clickablePoints);
                                clickablePoints = [];
                                _clickedPoint = null;
                            }

                            // remove from scene
                            scene.remove(object);
                            object.geometry.dispose();

                            // remove object from clickable list
                            for(let i = 0; i < clickable.length; i++){
                                if(clickable[i].name == object.name){
                                    clickable.splice(i, 1);
                                    break;
                                }
                            }

                            // reload object
                            loadObject(json);

                        }
                    });

                    break;

                // a deletion needs to be undone
                case 'undo_delete':

                    // reload the object
                    loadObject(json);

                    break;
            }

            // update status
            status = 'Aktion rückgängig gemacht';

            // update scene
            render();
        }

    })
    .catch(error => {

        console.log(error);
        // update status
        status = 'Fehler bei Rückgängig-Funktion';

    });
}

/*
 *
 */
export function cancelChanges(){

    // undo every action in log
    for(let i = log.length-1; i >= 0; i--){

        // get the object
        let object = scene.getObjectByName(log[i][1]);

        // undo last action in log
        // get type of change
        switch(log[i][0]){

            case 'newClass':

                // set old class
                object.userData[0] = log[i][2];

                // reset color
                updateColor(object);

                // update info
                objInfo = 'ID: ' + object.name.toString() + ', Klasse: ' + object.userData[0].toString();

                break;

            case 'translationObjekt':

                // get number of points
                let count = object.geometry.getAttribute('position').count;

                // for every point
                // every vertex needs to be changed to apply translation on bufferGeometry
                for(let j = 0; j < count; j++){

                    // translate
                    translateObject(object, j, -1*parseInt(log[i][3]), log[i][2]);

                }

                // update geometry
                object.geometry.attributes.position.needsUpdate = true;

                // check if object was selected
                if(_clicked && _clicked.name == object.name){

                    // reset points
                    removePoints(clickablePoints);
                    clickablePoints = [];
                    _clickedPoint = null;
                    loadClickablePoints(_clicked);

                }

                break;

            case 'translationPunkt':

                // move object back
                translateObject(object, parseInt(log[i][2]), -1*log[i][4], log[i][3]);

                // update geometry
                object.geometry.attributes.position.needsUpdate = true;

                // check if object was selected
                if(_clicked && _clicked.name == object.name){

                    // reset points
                    removePoints(clickablePoints);
                    clickablePoints = [];
                    _clickedPoint = null;
                    loadClickablePoints(_clicked);

                }

                break;

            case 'rotation':

                // move object back
                rotation(object, log[i][2], -1*log[i][3]);

                break;

        }

        // update status
        status = 'Änderungen verworfen';

        // update scene
        render();

    }

    // clear log
    log = [];
}

/***************************************save object as json************************************************************/

/*
 *  Takes an object an creates a json from its information
 *  @param object to create json from
 *  @return json if successful, null else
 */
function createJSON(object){

	try{

		// save vertices as arrays for x, y and z
		let x = [];
		let y = [];
		let z = [];

		// get vertices
		let vertices = object.geometry.getAttribute('position');

		// get number of vertices
		let count = object.geometry.getAttribute('position').count;

		// add coordinates to arrays
		for(let i = 0; i < count; i++){
			x.push(vertices.getX(i));
			y.push(vertices.getY(i));
			z.push(vertices.getZ(i));
		}

		// save vertex indices
		let faces = [];

		// get vertices as array
		let array = object.geometry.getIndex().array;
		let length = array.length;

		// store indices for each point in an array and add array to indices array
		for(let i = 0; i < length;){
			let row = [];
			row.push(array[i++],array[i++],array[i++]);
			faces.push(row);
		}

		// create JSON
		let json = {
			objID: object.name.toString(),
			objClass: object.userData[0].toString(),
			color: "",
			sanding: "",
			vertex_indices: faces,
			x: x,
			y: y,
			z: z
		};

		// return object as json
		return json;

	}catch(error){

        // update display
        status = 'Fehler bei JSON-Erstellung';

		// return null
		return null;
	}
}

/*****************************************render***********************************************************************/

/*
 *	Renders the scene and updates controls
 */
function render() {

	try{

	    // update display
        document.querySelector("#status").innerHTML = status;

        // update on object clicked
        if(_clicked){

            document.querySelector("#objInfo").innerHTML = objInfo;
            document.querySelector("#objInfo").style.color = 'black';
            document.querySelector('#deleteChosenObject').disabled = false;
            document.querySelector('#changeObjTitle').disabled = false;
            document.querySelector('#changedObjClass').disabled = false;
            document.querySelector('#translationSteps').disabled = false;
            document.querySelector('#translZ').disabled = false;
            document.querySelector('#translX').disabled = false;
            document.querySelector('#translY').disabled = false;
            document.querySelector('#saveChangedObject').disabled = false;
            document.querySelector('#cancelChangedObject').disabled = false;

            if(selectMode == 'Punkt'){

                // disable rotation
                document.querySelector('#rotationSteps').disabled = true;
                document.querySelector('#rotationZ').disabled = true;
                document.querySelector('#rotationX').disabled = true;
                document.querySelector('#rotationY').disabled = true;

            }
            else{

                // enable rotation
                document.querySelector('#rotationSteps').disabled = false;
                document.querySelector('#rotationZ').disabled = false;
                document.querySelector('#rotationX').disabled = false;
                document.querySelector('#rotationY').disabled = false;

            }

        }
        else{

          document.querySelector("#objInfo").textContent = 'Wähle ein Objekt...';
          document.querySelector("#objInfo").style.color = 'grey';
          document.querySelector('#deleteChosenObject').disabled = true;
          document.querySelector('#changeObjTitle').disabled = true;
          document.querySelector('#changedObjClass').disabled = true;
          document.querySelector('#translationSteps').disabled = true;
          document.querySelector('#translZ').disabled = true;
          document.querySelector('#translX').disabled = true;
          document.querySelector('#translY').disabled = true;
          document.querySelector('#rotationSteps').disabled = true;
          document.querySelector('#rotationZ').disabled = true;
          document.querySelector('#rotationX').disabled = true;
          document.querySelector('#rotationY').disabled = true;
          document.querySelector('#saveChangedObject').disabled = true;
          document.querySelector('#cancelChangedObject').disabled = true;

        }

		// update controls
		controls.update();

		// render scene
		renderer.render(scene, camera);
		requestAnimationFrame(render);

	}catch(error){

		//update display
        status = 'Fehler beim Rendern';

	}
}

/***************************************clickable points***************************************************************/

/*
 *  Creates a sphere on every vertex of an object and adds it to clickable points list
 *  @param object to create spheres at vertices
 */
function loadClickablePoints(object){

	try{

		// get number of points
		let count = object.geometry.getAttribute('position').count;

		// get vertices
		let vertices = object.geometry.getAttribute('position');

		// for every point
		for(let i = 0; i < count; i++){

		    // create a sphere on points position to show in scene
			let point = new THREE.Mesh( new THREE.SphereGeometry(0.05, 10, 10) , new THREE.MeshBasicMaterial( { color: 'red', transparent: true, opacity: 0.5 } ) );
			point.position.set(vertices.getX(i), vertices.getY(i), vertices.getZ(i));

			// save vertex index as name to have easy access later
			point.name = i.toString();

			// add to clickable points
			clickablePoints.push(point);

			// add to scene
			scene.add(point);
		}

	}catch(error){

        // update display
        status = 'Fehler beim Punkte-Laden';

	}
}

/****************************************remove************************************************************************/

/*
 *  Removes an object from scene
 *  @param object
 */
function removeFromScene(object){

	try{

	    // remove object from scene
		scene.remove(object);
		object.geometry.dispose();

	}catch(error){

		//update display
        status = 'Fehler beim Löschen';

	}
}

/*
 *  Removes every point an an array of points from the scene and clears the array
 *  @param array of points
 */
function removePoints(points){

	try{

	    // remove every point
		points.forEach(function(point){
			scene.remove(point);
			point.geometry.dispose();
		});
		// clear points array
		points = [];

	}catch(error){

		//update display
        status = 'Fehler beim Löschen';

	}
}

/****************************************highlighter*******************************************************************/

/*
 *  Resets objects material to state before being selected
 *  @param object
 */
function removeHighlighter(object){

	try{
	    // reset the material
		object.material.transparent = true;
		object.material.opacity = 0.5;

	}catch(error){

		//update display
        status = 'Fehler bei Markierung';

	}
}

/*
 *  Sets objects transparency and opacity to highlight it in scene
 *  @param object
 */
function setHighlighter(object){

	try{

	    // set transparency and opacity to highlight object
		object.material.transparent = false;
		object.material.opacity = 1.0;
		render();
	}catch(error){
		//update display
        status = 'Fehler bei Markierung';

	}
}

/*
 *  Removes lines from scene
 *  @param lines
 */
function removeLines(lines){

    try{

        // remove lines from scene
        scene.remove(lines);
        lines.geometry.dispose();

    }
    catch(error){

        // update status
        status = 'Fehler bei Löschen';

    }
}

/****************************************reset camera******************************************************************/

/*
 *  Gets the center of all objects in the scene
 *  Positions camera to look at scene center and have right rotation
 */
function lookAtScene(){

	try{

	    // get the center of all objects in the scene
		let box = new THREE.Box3();
		box.setFromObject(scene);
		let center = new Vector3();
		box.getCenter(center);

		// get the size of the bounding box around all objects
		let bbsize = new Vector3();
		box.getSize(bbsize);

		// set camera distance to bounding box size
		camera.position.y = bbsize.length();

		// three.js has z-vector looking at viewer, y-vector up and x-vector horizontal
		// rotate camera to have z-vector up
		camera.up = new Vector3(0,0,1);

		// have the camera look at scene center
		camera.lookAt(center);

	}catch(error){

		//update display
        status = 'Fehler bei Kamera-Setzung';

	}
}

/*
 *  Rotates the camera that z is up
 */
export function resetCamera(){

    // set the up vector to z
    camera.up = new Vector3(0,0,1);

}

/******************************************navigation******************************************************************/

/*
 *  Turns controls on or off, when user chooses it
 *  @param on or off
 */
export function changeNavigation(mode){

    // activate/deactivate controls
    controls.enabled = mode;
}

/****************************************load lines********************************************************************/

/*
 *  Creates lines between points to show the edges of an object
 *  @param points array
 */
function loadLines(points){

    try{

        // check if lines exist
        if(show_lines){

            // clear
            removeLines(show_lines);
            show_lines = null;

        }

        // load lines
        let geometry = new THREE.BufferGeometry();
        geometry.setAttribute('position', new THREE.Float32BufferAttribute(points, 3));
        let material = new THREE.LineBasicMaterial({color: 'red'});
        show_lines = new THREE.Line(geometry, material);

        // add lines to scene
        scene.add(show_lines);

    }
    catch(error){

        // update status
        status = 'Fehler bei Linien';

    }
}

/*******************************************show pointcloud************************************************************/

/*
 *
 */
export function showPointcloud(mode){

    pointcloud.visible = mode;

}

/**********************************************log*********************************************************************/

/*
 *
 */
export function logAction(entry){

    // add log entry
    log.push(entry);

}

/********************************************selection mode************************************************************/

/*
 *
 */
export function setSelectMode(mode){

    // set selection mode
    switch(mode){

        case 'Objekt':

            objInfo = 'Wähle Objekt...';
            selectMode = 'Objekt';
            break;

        case 'Punkt':

            objInfo = 'Wähle Punkt...';
            selectMode = 'Punkt';
            break;

        case 'Neu':

            objInfo = 'Setze Punkte...';
            selectMode = 'Neu';

            if(_clicked){

                // remove _clicked info
                removePoints(clickablePoints);
                clickablePoints = [];
                _clickedPoint = null;
                removeHighlighter(_clicked);
                _clicked = null;

            }

            break;

    }

}
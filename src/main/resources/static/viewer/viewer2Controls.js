
/**
 * @author Rebekka Lange
 */

// find the latest release here https://unpkg.com/three
import * as THREE from 'https://unpkg.com/three@0.125.1/build/three.module.js';
import { TrackballControls } from 'https://unpkg.com/three@0.125.1/examples/jsm/controls/TrackballControls.js';
import { Color } from 'https://unpkg.com/three@0.125.1/src/math/Color.js';
import { Box3 } from 'https://unpkg.com/three@0.125.1/src/math/Box3.js';
import { Vector3 } from 'https://unpkg.com/three@0.125.1/src/math/Vector3.js';

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
var mouse = new THREE.Vector2();

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
 *  Arrays to hold the objects and points clickable for the user
 */
var clickable = [];

/*
 *  Variables to store the current marked object
 */
export var _clicked;

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
 *  Status
 */
export var status = 'Start';

/*
 *
 */
var objInfo = 'Wähle ein Objekt...';

/**************************************load initial scene**************************************************************/

/*
 *  Call the functions to load all objects and the point cloud of the selected folder
 */
loadObjects();

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
	let material = new THREE.MeshBasicMaterial({color: 'gray', side: THREE.DoubleSide, transparent: true, opacity: 0.5});

	// create the object with material and geometry
	let object = new THREE.Mesh( geometry, material );

	// render only in visible
	object.frustumCulled = true;

	// save object id
	object.name = json.objID;

	// save object class
	let userData = [];
	userData.push(json.objClass);
	userData.push(json.color);
	userData.push(json.sanding);
	object.userData = userData;

    // update color
    setColor(object, json.color);

	// add object to clickable list if from class ground, ceiling or wall
	switch(object.userData[0]){

	    case 'wall':
	    case 'Wall':
	    case 'Wand':
	    case 'ground':
	    case 'Ground':
	    case 'Boden':
	    case 'ceiling':
	    case 'Ceiling':
	    case 'Decke':
	        clickable.push(object);
	        break;

	}

	// add object to scene
	scene.add(object);

}

/*************************************save in backend******************************************************************/

/*
 *
 */
export async function makePersistent(){

    // count errors
    let errorCount = 0;

    // remove highlighter
    if(_clicked){
        removeHighlighter(_clicked);
        _clicked = null;
    }

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

                    case 'newColor':

                        // set old class
                        object.userData[1] = log[i][2];

                        // reset color
                        setColor(object, object.userData[1]);

                        break;

                    case 'newSanding':

                        // set old class
                        object.userData[2] = log[i][2];

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

    objectSelection();

    // update status
    status = 'Objekt ausgewählt';

	// update scene
	render();
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
        if(_clicked && _clicked.name != intersects[0].object.name){

            // remove the highlighter from the last selected object
            removeHighlighter(_clicked);
            _clicked = null;

        }

        //  check if no object is currently selected
        if(!_clicked){

            // highlight newly selected object
            _clicked = intersects[0].object;
            setHighlighter(_clicked);

        }

        // update current objects information in gui
        objInfo = 'ID: ' + _clicked.name.toString() + ', Klasse: ' + _clicked.userData[0].toString() + ' , Farbe: ' + _clicked.userData[1].toString() + ' , Schleif-Grid: ' + _clicked.userData[2].toString();

        // update status
        status = 'Objekt gewählt';

    }
    else{

        if(_clicked){

            // remove highlighter
            removeHighlighter(_clicked);
            _clicked = null;

        }

    }
}

/*******************************************change color***************************************************************/

/*
 *
 */
export function setColor(object, color){

    try{

        // save color
        object.userData[1] = color;

        if(color != ""){

            // set the color
            object.material.color = new Color(color);

        }
        else{

            // default
            object.material.color = new Color('gray');

        }

        // update info
        objInfo = 'ID: ' + object.name.toString() + ', Klasse: ' + object.userData[0].toString() + ' , Farbe: ' + object.userData[1].toString() + ' , Schleif-Grid: ' + object.userData[2].toString();

        // update status
        status = 'Farbe geändert';

	}catch(error){

	    // update status
	    status = 'Farbe konnte nicht geändert werden';

	}

}

/**************************************change object class*************************************************************/

/*
 *  Retrieves the newly selected class for a selected object
 *  Calls PUT-method to send the changes to backend
 *  If the changes succeeded in backend, change the color of the object according to its new class
 *  If not, leave the old class
 */
export function setSanding(object, sanding){

    try{

        // save sanding
        object.userData[2] = sanding;

        // update info
        objInfo = 'ID: ' + object.name.toString() + ', Klasse: ' + object.userData[0].toString() + ' , Farbe: ' + object.userData[1].toString() + ' , Schleif-Grid: ' + object.userData[2].toString();

        // update status
        status = 'Schleif-Grid geändert';

	}catch(error){

	    // update status
	    status = 'Schleif-Grid konnte nicht geändert werden';

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

                // if a change needs to be undone
                case 'undo_changeObject':

                    // get the object
                    scene.children.forEach(function(object){

                        // check for the same id
                        if(object.name == json.objID){

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

            case 'newColor':

                // reset color
                setColor(object, log[i][2]);

                break;

            case 'newSanding':

                // move object back
                setSanding(object, log[i][2]);

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
			color: object.userData[1].toString(),
			sanding: object.userData[2].toString(),
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
            document.querySelector('#myColor').disabled = false;
            document.querySelector('#sanding').disabled = false;
            document.querySelector('#saveChangedObject').disabled = false;
            document.querySelector('#cancelChangedObject').disabled = false;

        }
        else{

            document.querySelector("#objInfo").textContent = 'Wähle ein Objekt...';
            document.querySelector("#objInfo").style.color = 'grey';
            document.querySelector('#myColor').disabled = true;
            document.querySelector('#sanding').disabled = true;
            document.querySelector('#saveChangedObject').disabled = true;
            document.querySelector('#cancelChangedObject').disabled = true;

        }

		// update controls
		controls.update();

		// render scene
		renderer.render(scene, camera);
		requestAnimationFrame(render);

	}catch(error){

        console.log(error);

		//update display
        status = 'Fehler beim Rendern';

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

	}catch(error){

		//update display
        status = 'Fehler bei Markierung';

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

/**********************************************log*********************************************************************/

/*
 *
 */
export function logAction(entry){

    // add log entry
    log.push(entry);

}
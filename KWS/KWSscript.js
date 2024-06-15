"use strict";


//Debugging console showing on website--------------------------

if (typeof console != "undefined")
  if (typeof console.log != 'undefined')
    console.olog = console.log;
  else
    console.olog = function () { };

console.log = function (message) {
  console.olog(message);
  document.getElementById("console").value += "\n" + message;
};
console.error = console.debug = console.info = console.log


/****
 * Dynamic attribute/class lists:
 * --Explorer figure:--
 * Attributes:
 * INDEX = Position in the main panel starting from the top left to the bottom right 
 * PATH = The path to the file or folder on the server side
 * 
 * ClassList:
 * EXPLORER_FIGURE
 *
 * --Root Panel "li" elements--
 * Attributes:
 * PATH = The path to the file or folder on the server side
 * 
 * ClassList:
 * 
 ****/
//for testing (will always ajax the testTree.xml). Setting it to false will enable real working with the Java server
var testBool = false;

//is set to true when on a mobile device and therefore 
var isMobileDevice = false;

//Desktop Vars
const workspace = document.getElementById("workspace")
const menuBarUl = document.getElementById("mainMenuBarUl")

//desktop Icons and their windows
const imgExplorer = document.getElementById("imgExplorer")
const explorer = document.getElementById("explorer")

//explorer panels
const explorerHeader = document.getElementById("explorerWindowDragFrame")
const explorerRootPanel = document.getElementById("explorerRootScroll")
const explorerMainPanel = document.getElementById("explorerMainPanel")
// const explorerMainPanelCanvas = document.getElementById("explorerMainPanelCanvas")
const explorerUploadPanel = document.getElementById("uploadPanel")

//menu bar buttons
const menuBackButton = document.getElementById("explorerMenuBack")
const menuNextButton = document.getElementById("explorerMenuForward")
const menuRefreshButton = document.getElementById("explorerMenuRefresh")
const menuDeleteButton = document.getElementById("explorerMenuDelete")
const menuCutButton = document.getElementById("explorerMenuCut")
const menuCopyButton = document.getElementById("explorerMenuCopy")
const menuPasteButton = document.getElementById("explorerMenuPaste")
const menuMkdirButton = document.getElementById("explorerMenuMkdir")
const menuUploadButton = document.getElementById("explorerMenuUpload")
const menuDownloadButton = document.getElementById("explorerMenuDownload")

const menuCloseExplorer = document.getElementById("closeExplorer")
const menuAddressBar = document.getElementById("pathAddressBar")

//other html elements
const fileSelect = document.getElementById("fileSelect")
const explorerRootUl = document.getElementById("rootListUL")


//css class list variables
const MAIN_MENU_BAR_LI = "mainMenuBarLi";
const MAIN_MENU_BAR_IMG = "mainMenuBarImg";

const DESKTOP_ELEMENT = "desktopElement";
const EXPLORER_ELEMENT = "explorerElement";

// const EXPLORER_ROOT_ELEMENT = "explorerRootElement";
const EXPLORER_ROOT_UL_ELEMENT = "explorerRootUlElement";
const EXPLORER_ROOT_LI_ELEMENT = "explorerRootLiElement";

const ROOT_EXPLORER_ICON = "rootExplorerIcon";
const ROOT_EXPLORER_ICON_OPEN = "rootExplorerIconOpen";
const ROOT_ELEMENT_WRAPPER = "RootElementWrapper";
const CURRENT_FOLDER = "currentFolder";

//class Strings
const EXPANDABLE = "expandable";  //the span elements on the explorer Root Panel, indicating, that folders are underlying and ready to be opened
const NESTED = "nested";          //opposite of ACTIVE; the ul element on the explorer Root Panel, indicates, that the folder (item) is collapsed
const ACTIVE = "active";          //opposite of NESTED; the ul element on the explorer Root Panel, indicates, that the folder (item) is expanded
const SELECTABLE = "selectable";  //the menu items on the explorer menu bar and in the context menu; indicates, that these buttons can be called
const CUT_ITEM = "cutItem";       //indicates, that the selected Items are cut and will be deleted, if the paste action is called afterwards at a different location

const DESKTOP_FIGURE = "desktopFigure"; //indicates, that the figure is located on the desktop
const EXPLORER_FIGURE = "explorerFigure"; //indicates, that the figure is located on the mainPanel in the explorer

const SEPERATOR = "seperator";
//String constants
const ROOT_PATH_NAME = "root"; //The name of the root element for the xml files (important, as the paths through the xml files are defined by their text and not their element)

const FOLDER_NAME = "folder";
const FILE_NAME = "file";

const INDEX = "index";

//image paths
const FOLDER_ICON_PATH = "pictures/folderIcon.png";
const FILE_ICON_PATH = "pictures/fileIcon.png";

// KWS commands. (defined in the webServer java project under main/KWSMethods.java)
// parameters are transmitted via a "?" after the command (see ajaxExplorerSub(header, command) function)

class KWS_CODES {

  static KWS = "KWS"; //Used instead of POST, GET etc. to indicate a KWS action
  static KWS_RESPONSE = "KWS-Response"; //a header sent by the server as a response, containing either fail or success 
  static SUCCESS = "success"; //one of the two KWS_RESPONSE values; indicates, that the command was successfully executed by the server
  static FAIL = "fail";//one of the two KWS_RESPONSE values; indicates, that the command failed somehow on the server side

  static GET_DIR_ELEMENTS = "getDirContent"; //requests the contents of a folder
  static DOWNLOAD = "download";//requests a file from the server to download
  static UPLOAD = "upload"; //uploads a file to the server
  static COPY_PASTE = "copyPaste"; //requests an action for the server to copy a file from a given location and paste it at a stated other location
  static DELETE = "delete"; //deletes the selected file from the server
  static RENAME = "rename"; //renames the selected file on the server
  static MKDIR = "mkdir"; //creates a new directory at the stated path with the stated name
  static MOVE = "move"; //moves the file on the server from on to another location

  static PATH = "path"; //the path of some operation or file or whatever
  static NAME = "name"; //the name indicator of sth, being it a file name or whatever
  static SOURCE = "source"; //indicating the source of sth, e.g. a copy paste action where to copy from
  static TARGET = "target"; //indicates the target of the operation, e.g. where to paste the current file
  static FILES_SIZE = "filesize"; //indicates the size of a file being uploaded or downloaded

}

//-----Desktop Window Vars-----

/**
 * manages the menu bar items, hence the open windows and 
 * their respective representatives in the Desktop menu at the bottom
 */
class DESKTOP_MENU_ITEMS {
  //contains all the current open windows as div elements, which are added to the menu bar at the very bottom of the screen
  static openWindows; //not yet in use

  static EXPLORER = "explorer";

  /**
   * Creates a shortcut in the shortcut menu bar at the bottom of the user interface.
   * @param {DESKTOP_MENU_ITEMS} item the kind of shortcut you want to create
   * @returns TRUE if the shortcut was created or FALSE if not (because shortcut exists already or so)
   */
  static createShortcut(item) {
    let crt = menuBarUl.getElementsByClassName(item)[0];

    if (crt != null) {
      return false;
    }


    //---create shortcut icon----
    let lin = document.createElement("li");
    lin.classList.add(MAIN_MENU_BAR_LI);
    lin.classList.add(item);

    let imgn = document.createElement("img");
    imgn.onclick = function () { ExplorerMethods.toggleExplorerWindow(); };
    imgn.classList.add(MAIN_MENU_BAR_IMG);
    imgn.title = item;
    imgn.alt = item;

    let figureSrc;
    switch (item) {
      case DESKTOP_MENU_ITEMS.EXPLORER:
        // pn.innerHTML = "Explorer";
        figureSrc = imgExplorer;
        break;
    }

    imgn.src = figureSrc.getElementsByTagName("img")[0].getAttribute("src");

    lin.appendChild(imgn);
    // divn.appendChild(pn);
    console.log(menuBarUl)
    menuBarUl.appendChild(lin);

    return true;
  }

  /**
   * 
   * @param {DESKTOP_MENU_ITEMS} item deletes the shortcut from the menu bar at the bottom of the user interface if present. 
   * @returns TRUE if the shortcut was successfully deleted or FALSE if failed to do so (because none is present or so)
   */
  static deleteShortcut(item) {
    //---check if shortcut icon exists and deletes it if needed

    let del = menuBarUl.getElementsByClassName(item)[0];

    if (del != null) {
      menuBarUl.removeChild(del);
      return true;
    }
    return false;
  }

  /**
   * creates a shortcut in the menu bar at the bottom of the screen or removes it.
   * @param {DESKTOP_MENU_ITEMS} item the kind of shortcut to be toggled
   */
  static toggleShortcut(item) {
    if (!this.deleteShortcut(item)) {
      this.createShortcut(item)
    }

    return;



    // //---check if shortcut icon exists and deletes it if needed
    // let del = menuBarUl.getElementsByClassName(item)[0];

    // if(del != null){
    //   menuBarUl.removeChild(del);
    //   return;
    // }


    // //---create shortcut icon----
    // let lin = document.createElement("li");
    // lin.classList.add(MAIN_MENU_BAR_LI);
    // lin.classList.add(item);

    // let imgn = document.createElement("img");
    // imgn.onclick = function () { ExplorerMethods.toggleExplorerWindow(); };
    // imgn.classList.add(MAIN_MENU_BAR_IMG);
    // imgn.title = item;
    // imgn.alt = item;

    // let figureSrc;
    // switch (item) {
    //   case DESKTOP_MENU_ITEMS.EXPLORER:
    //     // pn.innerHTML = "Explorer";
    //     figureSrc=imgExplorer;
    //     break;
    //   }

    //   imgn.src = figureSrc.getElementsByTagName("img")[0].getAttribute("src");

    //   lin.appendChild(imgn);
    //   // divn.appendChild(pn);
    //   console.log(menuBarUl)
    //   menuBarUl.appendChild(lin);
  }

}
//------explorer Variables-------

const ROOT_PATH = "/"; //important for Server and Client side, to decide on a common root element, which indicates the path before the drive letters
//The current Path where we are working. Always starts and ends with a "/"
var currentDir = ROOT_PATH;

/**
 * The item, which is currently selected (by clicking for example on it with the mouse).
 * contains the element or null if none is selected
*/
var currentSelectedItem = null;

/**
 * The list which stores the last folders the user has visited before to be able to iterate through them with back and forth buttons
 */
var lastDirList;


//-------------------------


/**
 * initiates the html document, by adjusting some sizes depending on the screen size,
 * loading event listeners etc.
 */
function init() {
  // initiate objects
  lastDirList = new DirectoryList(50);


  if (/Android|webOS|iPhone|iPad|Mac|Macintosh|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
    isMobileDevice = true;
  }
  // isMobileDevice = true;
  //create boundaries for the windows and its panels
  if (isMobileDevice) {
    //Fullscreen
    mobilePageInit();
    // explorer.style.width = window.innerWidth;
    // explorer.style.height = window.innerHeight;
  } else {
    // explorer.style.width = window.innerWidth / 2 +"px";
    // explorer.style.height = window.innerHeight / 2+"px";
    // console.log("aaaaaaaaa");
    // console.log(explorer.style.width);
    // console.log(explorer.style.height);


    //enable drag for the explorer window
    new WindowDragger(explorer, explorerHeader).enableDrag();

  }

  //add various event listener
  ResizerLib.startResizeListener();
  imgExplorer.onclick = function () {
    ExplorerMethods.toggleExplorerWindow();
    console.log("afasfajsjfash")
    DESKTOP_MENU_ITEMS.createShortcut(DESKTOP_MENU_ITEMS.EXPLORER);
  };
  imgExplorer.onmouseenter = function () { mouseEnterExplorerElement(this) };
  imgExplorer.onmouseleave = function () { mouseLeaveExplorerElement(this) };

  //menu bar item listeners
  menuCloseExplorer.onclick = function () { ExplorerActions.closeWindowAction() };
  menuBackButton.onclick = function () { ExplorerActions.lastDirAction() };
  menuNextButton.onclick = function () { ExplorerActions.nextDirAction() };
  menuRefreshButton.onclick = function () { ExplorerActions.refreshDirAction() };
  menuDeleteButton.onclick = function () { ExplorerActions.deleteAction() };
  menuCutButton.onclick = function () { ExplorerActions.cutAction() };
  menuCopyButton.onclick = function () { ExplorerActions.copyAction() };
  menuPasteButton.onclick = function () { ExplorerActions.pasteAction() };
  menuMkdirButton.onclick = function () { ExplorerActions.mkdirAction() };
  menuUploadButton.onclick = function () { ExplorerActions.uploadAction() };
  menuDownloadButton.onclick = function () { ExplorerActions.downloadAction() };

  explorerMainPanel.onmousedown = function (e) {
    if (e.target !== explorerMainPanel) { return };

    //if no item is selected and a click happens on the main panel, ignore it please
    if (getCurrentSelectedItems() !== null) {
      for (let ce of getCurrentSelectedItems()) {
        colorFigure(ce, null, null);
      }
      removeAllCurrentSelectedItems();
    }
  }

  new SelectorX(explorerMainPanel);
  ExplorerMethods.addDnDListener(explorerMainPanel,)
  document.addEventListener("keydown", keyListener);

  //init the XML Tree for the root panel
  RootXML.initRootXML();

  //initiate right click context menu
  ContextMenuLib.initContextMenu();

  console.log("------Initiation finished--------")
}



function mobilePageInit() {
  loadCSS("styleMobile.css");
  // document.body.innerHTML = '';

  // let mContainer = document.createElement("div");
  // let seperator = document.createElement("div");
  // let mMenu = document.createElement("div");
  // let mWorkspace = document.createElement("div");

  // mContainer.style.width = window.innerWidth;
  // mContainer.style.height = window.innerHeight;
  // mContainer.style.height = 1 +"fr";

  // mContainer.id = "mContainer"
  // seperator.classList.add(SEPERATOR);
  // mMenu.id = "mMenu";
  // mWorkspace.id = "mWorkspace";

  // mContainer.appendChild(mMenu);
  // mContainer.appendChild(seperator);
  // mContainer.appendChild(mWorkspace);

  // document.body.appendChild(mContainer);

  // // imgExplorer.remove();
  // mMenu.appendChild(imgExplorer);

  // // explorer.remove();
  // mWorkspace.appendChild(explorer);
}

/**
 * Load a new css style sheet for the webpage. 
 * Remember, loading a new style sheet might override some elements css values!
 * @param {STRING} url the url like "myFolder/myCss.css"
 */
function loadCSS(url) {
  // Get HTML head element
  var head = document.getElementsByTagName('HEAD')[0];

  // Create new link Element
  var link = document.createElement('link');

  // set the attributes for link element 
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = url;

  // Append link element to HTML head
  // document.body.appendChild(link); 
  head.appendChild(link);
  console.log("Mobile style sheet loaded")
}

//-------------------------

/**
 * makes a panel mouse selectable by oberlaying the panel with a canvas object when the mouse is pressed on the panel.
 * then it will draw a rectangle while the mouse moves from the starting point to the current position until the mouse is released.
 * To handle all the selection stuff in the lower instances, you might want to extend this class
 */
class Selector {

  // get references to the canvas and context
  panel;
  canvas;
  ctx;
  scaleX;
  scaleY;

  // calculate where the canvas is on the window
  // (used to help calculate mouseX/mouseY)
  canvasOffset;
  offsetX;
  offsetY;

  // this flage is true when the user is dragging the mouse
  isDown = false;

  // these vars will hold the starting mouse position
  startX;
  startY;
  width;
  height;


  moveListener = function (e) { self.handleMouseMove(e); }

  self;
  constructor(panel) {
    self = this;

    // style the context
    this.panel = panel;

    this.canvas = document.createElement("canvas");
    this.canvas.style.visibility = "hidden";
    this.canvas.style.position = "absolute";
    this.canvas.style.zIndex = "99";
    document.body.appendChild(this.canvas);

    this.ctx = this.canvas.getContext("2d");
    this.ctx.strokeStyle = "black";
    this.ctx.fillStyle = "rgba(0, 0, 0, 0.1)";
    this.ctx.lineWidth = 1;


    this.panel.addEventListener('mousedown', function (e) { self.handleMouseDown(e); });
    this.canvas.addEventListener('mouseup', function (e) { self.handleMouseUp(e); });
    // this.canvas.addEventListener('mouseout', function (e) { self.handleMouseOut(e); });


  }

  handleMouseDown(e) {
    if (e.target !== this.panel) { return; }

    document.addEventListener('mousemove', this.moveListener);

    // calculate where the canvas is on the window
    // (used to help calculate mouseX/mouseY)
    this.offsetX = this.#getOffsetLeft(this.panel);
    this.offsetY = this.#getOffsetTop(this.panel);


    this.canvas.style.width = this.panel.clientWidth + "px";
    this.canvas.style.height = this.panel.clientHeight + "px";
    this.canvas.style.left = this.offsetX + "px";
    this.canvas.style.top = this.offsetY + "px";

    //adjust scale factor, as canvas size does not match pixel size
    var rect = this.canvas.getBoundingClientRect();
    this.scaleX = this.canvas.width / rect.width;    // relationship bitmap vs. element for X
    this.scaleY = this.canvas.height / rect.height;  // relationship bitmap vs. element for Y

    this.canvas.style.visibility = "visible";


    // console.log('handleMouseDown')
    e.preventDefault();



    // save the starting x/y of the rectangle
    this.startX = parseInt(e.clientX - rect.left) * this.scaleX;
    this.startY = parseInt(e.clientY - this.offsetY) * this.scaleY;

    //set it to 0, as otherwise on a click without a mouse move, the old width and heigth will persist
    this.width = 0;
    this.height = 0;

    // set a flag indicating the drag has begun
    this.isDown = true;
  }

  handleMouseUp(e) {
    // console.log('handleMouseUp')
    document.removeEventListener('mousemove', this.moveListener);
    e.preventDefault();

    // the drag is over, clear the dragging flag
    this.isDown = false;

    // clear the canvas
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    this.canvas.style.visibility = "hidden";
  }

  handleMouseMove(e) {
    // console.log('handleMouseMove')
    e.preventDefault();

    // if we're not dragging, just return
    if (!this.isDown) {
      return;
    }

    // get the current mouse position
    let mouseX = parseInt(e.clientX - this.offsetX);
    let mouseY = parseInt(e.clientY - this.offsetY);

    // Put your mousemove stuff here

    // clear the canvas
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // calculate the rectangle width/height based
    // on starting vs current mouse position
    this.width = mouseX * this.scaleX - this.startX;
    this.height = mouseY * this.scaleY - this.startY;

    //check if cursor is out of canvas bounds on the x axis
    if ((this.startX + this.width) < 0) {//moving from right to left (negative width)
      this.width = -this.startX;
    } else if ((this.startX + this.width) > this.canvas.width) {
      this.width = this.canvas.width - this.startX;
    }

    //check if cursor is out of canvas bounds on the y axis
    if ((this.startY + this.height) < 0) {//moving from down to up (negative height)
      this.height = -this.startY;
    } else if ((this.startY + this.height) > this.canvas.height) {
      this.height = this.canvas.height - this.startY;
    }

    // draw a new rect from the start position 
    // to the current mouse position
    this.ctx.strokeRect(this.startX, this.startY, this.width, this.height);
    this.ctx.fillRect(this.startX, this.startY, this.width, this.height);
    // console.log(this.startX +" : " +this.startY  +" : " + width  +" : " + height)

  }




  #getOffsetLeft(elem) {
    var offsetLeft = 0;
    do {
      if (!isNaN(elem.offsetLeft)) {
        offsetLeft += elem.offsetLeft;
      }
    } while (elem = elem.offsetParent);
    return offsetLeft;
  }



  #getOffsetTop(elem) {
    var offsetTop = 0;
    do {
      if (!isNaN(elem.offsetLeft)) {
        offsetTop += elem.offsetTop;
      }
    } while (elem = elem.offsetParent);
    return offsetTop;
  }


}



/**
 * extends the Selector Class for this program. Basically it selects all elements in the main Explorer Window in correspondence where the selection was.
 */
class SelectorX extends Selector {
  parent;
  self;
  constructor(panel) {
    super(panel);

    this.self = this;
    // parent = new Selector(panel);
    this.canvas.addEventListener('mouseup', function () { self.figSelector(); });

  }

  /**
   * selects all figure elements which are inside the selected rectangle on the mouseup event
   */
  figSelector() {

    removeAllCurrentSelectedItems();

    // console.log((this.startX / this.scaleX + this.offsetX) + " : " + (this.startY / this.scaleY + this.offsetY) + " : " + (this.width / this.scaleX) + " : " + (this.height / this.scaleY))

    let selected = new Rectangle(this.startX / this.scaleX + this.offsetX, this.startY / this.scaleY + this.offsetY, this.width / this.scaleX, this.height / this.scaleY)
    for (let ce of this.panel.getElementsByTagName("figure")) {
      let eleBounds = ce.getBoundingClientRect();
      let cer = new Rectangle(eleBounds.left, eleBounds.top, eleBounds.width, eleBounds.height);
      if (selected.intersects(cer)) {
        // if (this.#isInsideRect(ce, this.startX / this.scaleX + this.offsetX, this.startY / this.scaleY + this.offsetY, this.width / this.scaleX, this.height / this.scaleY)) {
        selectExplorerElement(ce, true);
      }
    }
  }

}

/**
 * A simple class for a Rectangle which has some functions concerning intersection etc.
 */
class Rectangle {
  x = 0;
  y = 0;
  width = 0;
  height = 0;

  constructor(x, y, width, height) {

    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }


  /**
   * ADJUSTED VERSION FROM JAVA:
 * Determines whether or not this Rectangle and the specified
 * Rectangle intersect. Two rectangles intersect if
 * their intersection is nonempty.
 *
 * @param r the specified Rectangle
 * @return 'true' if the specified Rectangle
 *            and this Rectangle intersect;
 *            'false' otherwise.
 */
  intersects(r) {
    let tr = Rectangle.createPosRect(this);
    let tr2 = Rectangle.createPosRect(r);

    let tw = tr.width;
    let th = tr.height;
    let rw = tr2.width;
    let rh = tr2.height;
    if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
      return false;
    }
    let tx = tr.x;
    let ty = tr.y;
    let rx = tr2.x;
    let ry = tr2.y;
    rw += rx;
    rh += ry;
    tw += tx;
    th += ty;
    //      overflow || intersect
    return ((rw < rx || rw > tx) &&
      (rh < ry || rh > ty) &&
      (tw < tx || tw > rx) &&
      (th < ty || th > ry));

  }


  intersection(r) {
    //adjust the Rectangles for positive width and heigth values, as otherwise it wont compute correctly
    let tr = Rectangle.createPosRect(this);
    let tr2 = Rectangle.createPosRect(r);

    let leftX = Math.max(tr.x, tr2.x);
    let rightX = Math.min(tr.x + tr.width, tr2.x + tr2.width);
    let topY = Math.max(tr.y, tr2.y);
    let bottomY = Math.min(tr.y + tr.height, tr2.y + tr2.height);

    return new Rectangle(leftX, topY, rightX - leftX, bottomY - topY);


    //adjusted Java version, works as well
    // let tx1 = tr.x;
    // let ty1 = tr.y;
    // let rx1 = tr2.x;
    // let ry1 = tr2.y;
    // let tx2 = tx1; tx2 += tr.width;
    // let ty2 = ty1; ty2 += tr.height;
    // let rx2 = rx1; rx2 += tr2.width;
    // let ry2 = ry1; ry2 += tr2.height;
    // if (tx1 < rx1) tx1 = rx1;
    // if (ty1 < ry1) ty1 = ry1;
    // if (tx2 > rx2) tx2 = rx2;
    // if (ty2 > ry2) ty2 = ry2;
    // tx2 -= tx1;
    // ty2 -= ty1;
    // // tx2,ty2 will never overflow (they will never be
    // // larger than the smallest of the two source w,h)
    // // they might underflow, though...
    // if (tx2 < -Number.MAX_VALUE) tx2 = -Number.MAX_VALUE;
    // if (ty2 < -Number.MAX_VALUE) ty2 = -Number.MAX_VALUE;
    // let ret = new Rectangle(tx1, ty1, tx2, ty2);
    // return ret;
  }

  /**
   * creates a Rectangle with positive width and heigth values, by changing the start point (x and y values) in accordance
   * @param {Rectangle} r 
   * @returns the same Rectangle, but with positive heigth and width values, therefore moving the starting point (if not already) to the top left corner
   */
  static createPosRect(r) {
    if (r.width < 0) {
      r.x = r.x + r.width;
      r.width = -r.width
    }

    if (r.height < 0) {
      r.y = r.y + r.height;
      r.height = -r.height;
    }

    return r;
  }


}

// const FileSelector = (function () {

//   /**************
//   *
//   *   Public functions:
//   *
//   ***************/
//   return {
//     addListener(panel) {
//       panel.onclick = clicker;
//     }
//   }

//   /**************
// *
// *   Private functions:
// *
// ***************/

//   function clicker() {
//     //if no item is selected and a click happens on the main panel, ignore it please
//     if (getCurrentSelectedItems() !== null) {
//       for (let ce of getCurrentSelectedItems()) {
//         colorFigure(ce, null, null);
//       }
//       removeAllCurrentSelectedItems();
//     }
//   }

//   function dragger() {

//   }

// })();

//--------------------------

// class DirectoryMap {
//   curerntPoint = new Point(0, 0);

//   mapper = new Map();

//   constructor() {

//   }

//   addItem(item, x, y) {
//     this.mapper.set(new Point(x, y), item);
//   }

//   getItem(point) { return this.mapper.get(point) };

//   removeItem(point) { this.mapper.delete(point); }
// }

/**
 * a simple Point class, which contains x and y elements
 */
class Point {
  x = 0;
  y = 0;

  constructor(x = 0, y = 0) {
    this.x = x;
    this.y = y;
  }

  setX(x) { this.x = x; }
  setY(y) { this.y = y; }

  getX() { return this.x; }
  getY() { return this.y; }
}

/**
 * a class which is basically a sorted list and can be used to store items in a specific order.
 * Works like the directory list in every window explorer 
 */
class DirectoryList {
  lastDirectories = [];
  listSize = 20;
  iterator = 0;

  constructor(size = 20) {
    this.listSize = size;
    this.iterator = 0;
    this.lastDirectories = [];
  }

  /**
   * adds the path or whatever item you want to add to the list.
   * If the list still has capacity it will be added at the end.
   * If the capacity is full, will push every item one array down and element [0] will therefore be removed.
   * If the last element which was pushed is the same item, it will be ignored.
   * If the item is pushed when the iterator of this class is not the last item, will remove all element after the iterator element and add this item.
   * @param {String} path the path you want to add
   */
  push(path) {
    console.log("push dir \"" + path + "\" into last Dirs")

    //check if last entry is the same as the new path which is supposed to get pushed. If yes, ignore it.
    if (this.lastDirectories.length !== 0 && this.lastDirectories[this.lastDirectories.length - 1] === path) {
      console.log("same path")
      return;
    }

    if (this.iterator !== this.lastDirectories.length - 1) {
      console.log("comming from back to new path")
      //delete all paths after the one where our iterator is, for example when you hit the back button a few times and then enter another path you will end up here
      while (this.iterator + 1 < this.lastDirectories.length) {
        this.lastDirectories.splice(this.iterator + 1, 1);
      }
      this.lastDirectories.push(path);
      this.iterator = this.lastDirectories.length - 1;
    } else if (this.lastDirectories.length < this.listSize) {
      //in case there is still space in the list, push the entry
      console.log("list not full yet")

      this.lastDirectories.push(path);
      this.iterator = this.lastDirectories.length - 1;
    } else {
      console.log("list full, omitting first")
      //push every entry one element further down. The one at position 0 will be removed
      for (let t in this.lastDirectories) {
        if (t !== 0) {//skip the first element, which can not be pushed further down
          this.lastDirectories[t - 1] = this.lastDirectories[t];
        }
      }
      this.lastDirectories[this.lastDirectories.length - 1] = path;
    }


  }

  /**
   * returns the element previous to the current selected one (iterator will move one item back)
   * will return the same element as the current item, when iterator is already on the start of the list.
   */
  getPreviousDir() {

    if (this.iterator > 0) {
      this.iterator--;
    }
    console.log("prev Dir")
    // console.log(this.lastDirectories)
    // console.log(this.iterator)
    return this.lastDirectories[this.iterator];
  }

  /**
   * returns the next element to the current element (iterator wil move one item forth).
   * Will return the same element as the current item, when end of the list is reached.
   */
  getNextDir() {

    if (this.iterator < this.lastDirectories.length - 1) {
      this.iterator++;
    }
    console.log("next Dir")
    // console.log(this.lastDirectories)
    // console.log(this.iterator)
    return this.lastDirectories[this.iterator];

  }


}

/**
 * Handles all the requests to the server
 */
const AJAXLib = (function () {

  /**************
  *
  *   Public functions:
  *
  ***************/
  return {


    /**
     * Download a file from the server given the specific file path
     * @param {String} path the path to the file you want to download
     */
    downloadFile: function (path) {
      let ajax = AJAXLib.ajaxKWS(addParameter(KWS_CODES.DOWNLOAD, KWS_CODES.PATH, path), false, downloadResponse);

      ajax.responseType = "blob";
      console.log("chanign response type to blob for incomming file to download")

      ajax.send();
    },

    /**
     * Sends an ajax request to the server to delete the file stated in the parameter
     * @param {String} path the path of the file/folder to delete
     */
    deleteFile: function (path) {
      AJAXLib.ajaxKWS(addParameter(KWS_CODES.DELETE, KWS_CODES.PATH, path), true, function () { AJAXLib.ajaxExplorerDir() });
    },
    /**
     * Sends an ajax request to the server to copy the file/folder stated in the fromPath parameter to the toPath parameter
     * @param {String} fromPath path of a file to copy
      * @param {String} toPath path where the pasted file should be
      * @returns the ajax element, which has to be called send() to send the request
      */
    copyPaste: function (fromPath, toPath) {
      return AJAXLib.ajaxKWS(addParameter(addParameter(KWS_CODES.COPY_PASTE, KWS_CODES.SOURCE, fromPath), KWS_CODES.TARGET, toPath), false, function () { AJAXLib.ajaxExplorerDir() });
    },

    /**
     * Moves on file or folder to the target folder and refreshes the dir when done
     * @param {String} sourcePath the file or folder to move
     * @param {String} targetPath the target folder you want to move to
     */
    moveTo: function (sourcePath, targetPath) {
      AJAXLib.ajaxKWS(addParameter(addParameter(KWS_CODES.MOVE, KWS_CODES.SOURCE, sourcePath), KWS_CODES.TARGET, targetPath), true, function () { AJAXLib.ajaxExplorerDir() });
    },

    /**
* calls an ajax request to get the current files and folders of the directionry requested 
* and set this at the current dir in the explorer window
* @param {String} dir  (optional, default=currentDir) the path of the folder to get the elements from. If not stated, will update the current directory
* @param {Boolean} addToLastDir (optional, default=true) if true, it will be added to the lastDirList, which contains the last visited paths
*/
    ajaxExplorerDir: function (dir = currentDir, addToLastDir = true) {
      if (testBool) {
        console.log("TEST BOOL ACTIVE")

        //call ajax for current files and folders in directory as XML

        let ajax = new XMLHttpRequest();

        ajax.onreadystatechange = function () {
          dirContentResponse(this, false);
        }

        ajax.open("GET", "./testTree.xml", true);
        ajax.send();



        // AJAXLib.ajaxKWS("GET", "./testTree.xml", false, function (ajax) { dirContentResponse(ajax, addToLastDir) });//for testing
        // AJAXLib.ajaxKWS(addParameter(KWS_CODES.GET_DIR_ELEMENTS, KWS_CODES.PATH, dir), true, function (ajax) { dirContentResponse(ajax, addToLastDir) });
        return;
      }


      console.log("setting cdir to " + dir);
      currentDir = dir;
      menuAddressBar.innerHTML = dir;

      AJAXLib.ajaxKWS(addParameter(KWS_CODES.GET_DIR_ELEMENTS, KWS_CODES.PATH, dir), true, function (ajax) { dirContentResponse(ajax, addToLastDir) });
    },

    /**
     * Rename the file/folder to the new name
     * @param {String} path The path of the file/folder to rename
     * @param {String} newName the new name (including extensions if file (eg.g. ".txt"))
     */
    renameRequest: function (path, newName) {
      AJAXLib.ajaxKWS(addParameter(addParameter(KWS_CODES.RENAME, KWS_CODES.PATH, path), KWS_CODES.NAME, newName), true, function () { AJAXLib.ajaxExplorerDir() });
    },

    /**
     * Requests, that a new folder with the stated name will be created at the stated path
     * @param {String} path the path where you want to create your new Folder
     * @param {String} newFolderName the name of the folder which should be created
     */
    mkdirRequest: function (path, newFolderName) {
      AJAXLib.ajaxKWS(addParameter(addParameter(KWS_CODES.MKDIR, KWS_CODES.PATH, path), KWS_CODES.NAME, newFolderName), true, function () { AJAXLib.ajaxExplorerDir() });
    },


    /** 
     * 
     * @param {XMLHttpRequest} ajax 
     * @returns true when the KWS_Response Header sends "success" (will be send when an action was performed successfully on the server side)
     */
    isSuccess: function (ajax) {
      let resp = ajax.getResponseHeader(KWS_CODES.KWS_RESPONSE);
      if (resp === KWS_CODES.SUCCESS) {
        return true;
      }
      return false;
    },



    /**
     * send an ajax request to the server with the given header and directory parameter.
     * After obtaining the sent xml object, it will evaluate it by updating the explorer root Tree and the main panel
     * If parameters are needed for the command, they will be transmitted via a "?"".
     * More parameters are transmitted via a "&" between the parameters (see ajaxExplorerSub(header, command) function)
     * Parameters of a command have to follow a "?" after the {@link KWS_CODES} as the command
     * and the parameters itself need to be in the form ("parameter=value") to be corecctly interpreted.
     *
     * @param {String} command the command and optional parameters (e.g. getDirContent?dir=C:/aPath)
     * @param {Boolean} autoSend OPTIONAL (default true) : sends the request when true. If false, allows modification of the ajax element before sending (call this element.send() function manually)
     * @param {function} responseFunc OPTIONAL (default empty function) : the function which gets called upon a successfull response. It takes the ajax element as a parameter (hence it is calling responseFunc(this)) 
     * @returns the XMLHttpRequest (AJAX) element
     */
    ajaxKWS: function (command, autoSend = true, responseFunc = function () { }) {
      // check if we are at a root were only the drive letters are listed. In this case ignore all commands except the get directory elements to traverse through the drives 
      if (currentDir === ROOT_PATH && !command.includes(KWS_CODES.GET_DIR_ELEMENTS)) {
        return;
      }


      let header = KWS_CODES.KWS;

      console.log("new Ajax Request sent: header=\"" + header + "\" command=\"" + command)

      let ajax = new XMLHttpRequest();

      //setting appropriate response Type if neccessary
      // if (command.startsWith(KWS_CODES.DOWNLOAD)) {
      //   xhttp.responseType = "blob";
      //   console.log("chanign response type to blob")
      // }
      ajax.onreadystatechange = function () {

        if (isReady(this)) {
          console.log("ajax well done!")
          console.log(this.getAllResponseHeaders())
          if (this.getResponseHeader(KWS_CODES.KWS_RESPONSE) !== KWS_CODES.SUCCESS) {
            alert("failed to execute the action: " + command)
          }

          // console.log(this.responseXML)
          // console.log(this.responseText)

          // this.responseText = this.responseText.replace("&")

          console.log("executing response function");
          responseFunc(this);

          // if (!testBool) {
          // console.log("looking for appropriate ajax response");
          // switch (this.getResponseHeader(KWS_CODES.KWS_RESPONSE)) {
          // case KWS_CODES.DOWNLOAD:
          //   downloadResponse(this);
          //   break;

          //   case KWS_CODES.GET_DIR_ELEMENTS:
          //     dirContentResponse(this);
          //     break;
          // }
        }

      };

      ajax.open(header, command, true);

      if (autoSend) {
        ajax.send();
      }

      return ajax;
    },

    sendPOSTTest: function () {

      let xhr = new XMLHttpRequest();

      // let ts = "Test string to send!!\n\\n or so\r"+10+"aa";
      let ts = "Test string to send!!\n\n";
      xhr.open("POST", addParameter(addParameter(KWS_CODESUPLOAD, KWS_CODES.PATH, "Test.txt"), "fileSize", ts.length), true);
      console.log(ts);

      xhr.send(ts);
      console.log("allsent!!!!!!!!!!!!!!!!!!")
    },



    /**
     * Uploads the files to the path where the current directory is (set in currentDir)
     * @param {FILE} file the file to upload
     */
    fileUpload: function (file) {
      let reader = new FileReader();
      let pb = new ProgressBar(file.name, 100, true);
      let xhr = new XMLHttpRequest();

      let pbn = explorerUploadPanel.appendChild(pb.getElement());
      explorerUploadPanel.style.visibility = "visible";

      xhr.onreadystatechange = function () {
        console.log("response");
        console.log(this.getAllResponseHeaders())
        console.log(this.responseText);
      }


      xhr.upload.addEventListener("progress", function (e) {
        if (e.lengthComputable) {
          const percentage = Math.round((e.loaded * 100) / e.total);
          pb.update(percentage);
        }
      }, false);

      xhr.upload.addEventListener("load", function () {
        pb.update(100);
        // const canvas = self.pb.ctx.canvas;
        // canvas.parentNode.removeChild(canvas);
        console.log("upload complete")

        //wait 5 seconds and then remove the progress bar 
        setTimeout(() => {
          console.log("remover")
          console.log(explorerUploadPanel.hasChildNodes)
          console.log(explorerUploadPanel.childNodes.length)
          explorerUploadPanel.removeChild(pbn);
          if (!explorerUploadPanel.hasChildNodes) {
            console.log("hide")
            explorerUploadPanel.style.visibility = "hidden";
          }
        }, 5000);

        AJAXLib.ajaxExplorerDir();
      }, false);

      reader.onload = function (evt) {
        console.log(addParameter(addParameter(KWS_CODES.UPLOAD, KWS_CODES.PATH, currentDir + file.name), KWS_CODES.FILES_SIZE, evt.target.result.length))
        xhr.open("POST", addParameter(addParameter(KWS_CODES.UPLOAD, KWS_CODES.PATH, currentDir + file.name), KWS_CODES.FILES_SIZE, evt.target.result.length), true);

        //neccessary to end the POST body with "\n\n", as the body is read with a BufferedReader on the other side and will finish reading the body if there is a line with the length of 0
        //See the Java Server File getPOSTbody() for details
        xhr.send(evt.target.result + "\n\n");
      };

      //IMPORTANT!!! Reads the file as a base64 STring which will be transmitted and decoded on the other side. Using another method to encode the file will result in decode error on the Server side!!!
      reader.readAsDataURL(file);
    }



  }

  /**************
  *
  *   Private functions:
  *
  ***************/

  function isReady(ajax) {
    //ready state == 4 means the request was sucessfully pulled
    //status == 200 means this page was loaded correctly
    if (ajax.readyState === 4 && ajax.status === 200) {
      return true;
    }
    return false;
  }
  /**
   * Adds parameter to the command by adding a "?" to the command and then the parameter, followed by
   * a "=" and then the value. If parameters are already attached, adds a "&" instead of a "?"
   * @param {String} command 
   * @param {String} parameter 
   * @param {String} value 
   * @returns the command with the parameters attached, ready to ajax
   */
  function addParameter(command, parameter, value) {
    if (!command.includes("?")) {
      command += "?";
    } else { command += "&"; }

    command += encodeCharacters(parameter) + "=" + encodeCharacters(value);
    return command;

  }

  /**
   * Encodes special characters like "&", "=", "?", to their equivalent escape string to be able to transmit these values over url without misinterpretation. 
   * @param {String} str the string to change the escapeable characters
   */
  function encodeCharacters(str) {
    if (str === null) {
      console.warn("WHY IS THERE A NULLER IN THE ENCODING PARAMETER???")
    }
    //make sure it is a String (e.g. for a number (which will be treated as an int and not as a String) as no Types in JS :( ))
    str = str.toString();

    console.log("encode : " + str)
    str = str.replaceAll("&", "%24");
    str = str.replaceAll("?", "%3F");
    str = str.replaceAll("=", "%3D");
    console.log(str)
    return str;
  }


  /**
   * Handles the response of a directory content update
   * @param {AJAX} ajax 
   */
  function dirContentResponse(ajax, addToLastDir = true) {
    console.log("updating Dir elements")

    // as the currentElement is deleted by the following operation 
    // (internal html of the main panel is totally replaced),
    //  no item will be selected and file operation need to be disabled
    ContextMenuLib.disableFileOperations();
    removeAllCurrentSelectedItems();

    ExplorerMethods.updateExplorerRootPanel(ajax.responseXML);
    ExplorerMethods.updateExplorerMainPanel(ajax.responseXML);

    //on successfull entering directory, add the path to the last visited lists
    if (addToLastDir) {
      let dir = ajax.responseXML.getElementsByTagName("root")[0].getAttribute("path");
      lastDirList.push("/" + dir);
    }
  }

  /**
   * Handles the incomming file data to download
   * @param {AJAX} ajax 
   */
  function downloadResponse(ajax) {

    console.log("downloading given file")
    let blob = ajax.response;
    let fileName = ajax.getResponseHeader(KWS_CODES.NAME);

    console.log(fileName)
    // console.log(ajax.getAllResponseHeaders)
    // console.log(ajax.responseText)
    //Check the Browser type and download the File.
    let isIE = false || !!document.documentMode;
    if (isIE) {
      window.navigator.msSaveBlob(blob, fileName);
    } else {
      let url = window.URL || window.webkitURL;
      let link = url.createObjectURL(blob);
      var a = document.createElement("a");
      a.setAttribute("download", fileName);
      a.setAttribute("href", link);
      document.body.appendChild(a);

      a.click();
      document.body.removeChild(a);
    }
    console.log("download done`?")

  }


})();





class ProgressBar {
  //neccessary for creating always a unique id to match the "for" Attribute of the labels to the progressbar
  static pb_id_counter = 0;

  element = document.createElement("div");
  plabel = document.createElement("label");
  bar = document.createElement("progress");
  name = "";

  showProgressPercentage = true;

  constructor(name, maxValue, showProgressAsText) {
    this.showProgressPercentage = showProgressAsText;
    this.name = name;

    this.bar.id = "progressbar" + ProgressBar.pb_id_counter;
    this.bar.max = maxValue;
    this.bar.value = 0;


    ProgressBar.counter++;
    this.plabel.setAttribute("for", "progressbar" + ProgressBar.pb_id_counter);

    this.element.appendChild(this.plabel);
    this.element.appendChild(this.bar);

    //initiate label
    this.update(0);

  }

  getElement() {
    return this.element;
  }

  update(value) {
    this.bar.value = value;
    this.plabel.innerHTML = this.name;
    if (this.showProgressPercentage) {
      this.plabel.innerHTML += " " + value + "%";
    }

    //add a bit of space between the label and the progressbar
    this.plabel.innerHTML += " "
  }

}


const ExplorerActions = (function () {

  //arrays of to cut or copy items
  let copyItems = null;
  let cutItems = null;

  /**************
   *
   *   Public functions:
   *
   ***************/
  let menuBarItems = [menuDeleteButton,
    menuCutButton, menuCopyButton,
    menuDownloadButton];

  return {
    getMenuBarItems: function () { return menuBarItems },

    lastDirAction: function () {
      AJAXLib.ajaxExplorerDir(lastDirList.getPreviousDir(), false);
    },

    nextDirAction: function () {
      AJAXLib.ajaxExplorerDir(lastDirList.getNextDir(), false);
    },


    refreshDirAction: function () {
      AJAXLib.ajaxExplorerDir();
    },


    deleteAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      let confirmString = "Are you sure you want to delete these elements?\n";

      if (ExplorerActions.itemIsSelected()) {
        let type;
        let fname;

        for (let ce of getCurrentSelectedItems()) {
          fname = ce.getAttribute(KWS_CODES.PATH)

          //if folder, the path ends with "/", so remove that to get the folders name after the if statement
          if (fname.endsWith("/")) {
            type = "Folder";
            fname = fname.substring(0, fname.length - 1);
          } else {
            type = "File";
          }

          fname = fname.substring(fname.lastIndexOf("/") + 1);

          confirmString += type + " \"" + fname + "\", \n";
        }
        //remove the last ", " and add a "?"
        confirmString = confirmString.substring(0, confirmString.length - 3);

        if (confirm(confirmString)) {
          for (let ce of getCurrentSelectedItems()) {
            AJAXLib.deleteFile(ce.getAttribute(KWS_CODES.PATH));
          }
        }
      }
      else {
        console.log("could not delete file, as no file is selected")
      }
    },


    cutAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      if (ExplorerActions.itemIsSelected()) {
        console.log("cut file")
        copyItems = null;

        //remove the previous cut object
        if (cutItems !== null) {
          cutItems.classList.remove(CUT_ITEM);
        }
        cutItems = getCurrentSelectedItems();
        cutItems.classList.add(CUT_ITEM);
      } else {
        console.log("no element selected, could not cut")

      }
      checkPasteAction();
    },


    copyAction: function () {
      console.log("COPY")
      console.log(currentDir)
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      if (ExplorerActions.itemIsSelected()) {
        console.log("copy action called")
        copyItems = getCurrentSelectedItems();
        if (cutItems !== null) {
          for (ce of getCurrentSelectedItems()) {
            cutItems.classList.remove(CUT_ITEM);
          }
          cutItems = null;
        }
      } else {
        console.log("no element selected, could not copy")
      }
      checkPasteAction();
    },


    pasteAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      if (copyItems !== null) {
        for (let cce of copyItems) {
          console.log("copy paste file")
          let copyPath = cce.getAttribute(KWS_CODES.PATH);
          //targetPath is the current directory + the files name (copy path ends with "/", so we first need get rid of this to get the last index of "/")
          if (copyPath.endsWith("/")) {
            //directory
            copyPath = copyPath.substring(0, copyPath.length - 1); // remove the last "/" to get the folders name in the targetPath statement
          }
          let targetPath = currentDir + copyPath.substring(copyPath.lastIndexOf("/") + 1);

          AJAXLib.copyPaste(copyPath, targetPath).send();
        }
      } else if (cutItems !== null) {
        for (let cce of cutItems) {

          let cutPath = cce.getAttribute(KWS_CODES.PATH);
          console.log("cut paste file")
          console.log(cutPath)
          let targetPath = currentDir + cutPath.substring(cutPath.lastIndexOf("/") + 1);
          let ajax = AJAXLib.copyPaste(cutPath, targetPath);

          // AJAXLib.isSuccess(ajax);

          //on successful copying delete the source file afterwards
          ajax.onreadystatechange = function () {
            if (AJAXLib.isSuccess(this)) {
              AJAXLib.deleteFile(cutPath); //after copy action delete the source file as defined with the cut action
              cutPath = null; //file was deleted, so source is not existent anymore
            }
          }
          ajax.send();
        }
      } else {
        console.log("no paste action taken, as no source was found or we are at the root");
      }
      checkPasteAction();
    },


    mkdirAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      let path = currentDir;
      let newFolderName = prompt("Please enter a new Filename", path.substring(path.lastIndexOf("/") + 1));

      //only in case the prompt was not cancelled
      if (newFolderName !== null) {
        AJAXLib.mkdirRequest(path, newFolderName);
      }
    },


    renameAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      if (ExplorerActions.itemIsSelected()) {
        let path = getCurrentSelectedItems()[0].getAttribute(KWS_CODES.PATH);
        let newName = prompt("Please enter a new Filename", path.substring(path.lastIndexOf("/") + 1));

        //only in case the prompt was not cancelled
        if (newName !== null) {
          AJAXLib.renameRequest(path, newName);
        }
      } else {
        console.log("no element selected, could not rename")
      }
    },


    uploadAction: function () {
      //check if we are at the root and if yes, return
      if (currentDir === ROOT_PATH) { return; }

      console.log("upload start")
      let listener = function () {
        console.log(fileSelect.files.length);
        console.log(fileSelect.files[0]);
        for (var cf of fileSelect.files) {
          AJAXLib.fileUpload(cf);
        }
        fileSelect.removeEventListener("change", listener.bind(this));

        //remove the informations about the last selected file, as otherwise the event listener will not fire on change when the same file is selected again
        fileSelect.value = "";
      };

      fileSelect.onchange = listener;
      fileSelect.click();
    },

    downloadAction: function () {
      //check if we are at the root and if yes, return
      console.log("download???")
      console.log(currentDir === ROOT_PATH)
      console.log(ExplorerActions.itemIsSelected())
      if (currentDir === ROOT_PATH) { return; }

      if (ExplorerActions.itemIsSelected()) {
        console.log("-as-sad-sadasdasd-asd")
        console.log(getCurrentSelectedItems())
        for (let ce of getCurrentSelectedItems()) {
          AJAXLib.downloadFile(ce.getAttribute(KWS_CODES.PATH));
        }
      }
    },


    /**
     * makes the explorer window dissappear when clicking on the close (x) button
     */
    closeWindowAction: function () {
      DESKTOP_MENU_ITEMS.deleteShortcut(DESKTOP_MENU_ITEMS.EXPLORER);
      ExplorerMethods.toggleExplorerWindow();
      // explorer.style.visibility = "hidden";

      //reset vars
      currentDir = ROOT_PATH;
      currentSelectedItem = null;
      lastDirList = [];
    },

    itemIsSelected: function () {
      return (getCurrentSelectedItems() !== null)
    }



  }

  /**
   * checks if paste action is available and if yes, will make the paste button selectable, otherwise grey it out
   * @returns true if paste action is available
   */
  function checkPasteAction() {
    if (copyItems === null && cutItems === null) {
      menuPasteButton.classList.remove(SELECTABLE);
      getContextMenuPasteItem().classList.remove(SELECTABLE);
      return false;
    }
    ContextMenuLib.getContextMenuPasteItem().classList.add(SELECTABLE);
    menuPasteButton.classList.add(SELECTABLE);
    return true;

  }



})();

//--------------------------

const ExplorerMethods = (function () {

  /**************
  *
  *   Public functions:
  *
  ***************/
  return {

    /**
     * Opens the explorer window, by setting its visible status to "visible" or hides it by setting its value to "hidden".
     * Basically toggles the window from MINIMIZED to EXPANDED
     */
    toggleExplorerWindow: function () {
      // AJAXLib.POSTTest();

      if (explorer.style.visibility === "hidden" || explorer.style.visibility === "") {
        collapseExplorerFolders();

        AJAXLib.ajaxExplorerDir(ROOT_PATH);//for real requests (always start at the root (computer) when opening the explorer anew)

        if (!isMobileDevice) {
          //adjust window size and location depending on screen size
          explorer.style.width = workspace.clientWidth * 4 / 5 + "px";
          explorer.style.height = workspace.clientHeight * 4 / 5 + "px";

          explorer.style.left = workspace.clientWidth / 2 - explorer.clientWidth / 2 + "px";
          explorer.style.top = workspace.clientHeight / 2 - explorer.clientHeight / 2 + "px";

          console.log(workspace.clientHeight)
          console.log(workspace.clientWidth * 4 / 5)

        } else {
          explorer.style.width = workspace.clientWidth + "px";
          explorer.style.height = workspace.clientHeight + "px";

          explorer.style.left = 0;
          explorer.style.top = 0;

          // console.log("STAAAAAAAAAAAART")
          // console.log(explorer.style.width)
          // console.log(explorer.style.height)
          // console.log(workspace.clientWidth)
          // console.log(workspace.clientHeight)


        }

        explorerRootPanel.style.width = explorer.clientWidth / 5 + "px";
        explorerHeader.style.height = explorer.clientHeight / 5 + "px";

        explorer.style.visibility = "visible"
      } else {
        explorer.style.visibility = "hidden"
      }

    },


    //adjust explorer Table---------------------------------------

    /**
     * Takes the xml file (as String/text format) and parse it into the explorer window as the new directory lineup
     * @param {String} xml 
     */
    updateExplorerMainPanel: function (xml) {

      //get all the folder and file names from the recieved xml object
      var folders = RootXML.getXMLelements(xml, FOLDER_NAME);
      var files = RootXML.getXMLelements(xml, FILE_NAME);

      //delete previous content
      explorerMainPanel.innerHTML = "";

      let eles = [];
      //add new file and folder elements
      for (var ci of folders) {
        eles.push(createExplorerItem(ci.childNodes[0].nodeValue, FOLDER_NAME));
        // explorerMainPanel.appendChild();
      }
      for (var ci of files) {
        eles.push(createExplorerItem(ci.childNodes[0].nodeValue, FILE_NAME));
        // explorerMainPanel.appendChild(createExplorerItem(ci.childNodes[0].nodeValue, FILE_NAME));
      }

      let index = 0;
      for (let ce of eles) {
        ce.setAttribute(INDEX, index);
        index++;
        explorerMainPanel.appendChild(ce);
      }
    },

    /**
 * updates the internal XML object with the new information stated in this xml. Also extends the rootPanel Tree if necessary.
 * @param {XML} xml the XML object with new Information regaring a specific directory, which you probably got from the ajax
 */
    updateExplorerRootPanel: function (xml) {

      //check if the root element is existent, if not exit immediately
      var p = xml.getElementsByTagName("root")
      if (p.length == 0) { return; }

      RootXML.updateRootTableXML(xml);//update the internal XML object with the new information
      console.log("root explorer update complete");

      // showXMLasAlert(rootTableXML);
      buildTree(RootXML.getRootXML(), explorerRootUl);//build the root panel tree up from scatch
      console.log("building explorer Tree finito");
    },


    addDnDListener: function (element, isDraggable = true, isDropable = true) {
      if (isDraggable) {
        element.draggable = true;

        //make the element dropable
        element.addEventListener("dragover", function (ev) {
          ev.preventDefault();
        });

        element.addEventListener("dragstart", function (ev) {
          //copy the path of the file/folder to drag

          ev.dataTransfer.setData("text", ev.target.getAttribute(KWS_CODES.PATH));
          // console.log("set datatransfer")
          // console.log(ev.target.getAttribute(KWS_CODES.PATH))
          // console.log(ev.target)
          // console.log(ev.src)
          // console.log(ev.dataTransfer.getData("text"))
        });
      }

      if (isDropable) {

        element.addEventListener("drop", function (ev) {
          ev.preventDefault();
          console.log("dropped")
          var sourcePath = ev.dataTransfer.getData("text");
          var targetPath = ev.target.getAttribute(KWS_CODES.PATH);

          var files = ev.dataTransfer.files; // Array of all files dropped from the external window

          console.log("WTF")
          console.log(sourcePath)
          console.log(targetPath)
          // console.log(ev.target)
          // console.log(ev.src)
          // console.log(ev)
          console.log(files.length)

          if (files.length !== 0) {
            //upload files dragged directly from an external window onto the browser
            for (var i = 0, file; file = files[i]; i++) {
              AJAXLib.fileUpload(file);
            }

          } else if (sourcePath !== null && targetPath !== null) {
            //make an internal drag and drop
            AJAXLib.moveTo(sourcePath, targetPath);
          } else { console.log("drag n drop nuller found: src=" + sourcePath + "; target=" + targetPath) }
        });

      }
    }


  }


  /**************
  *
  *   private functions:
  *
  ***************/


  /**
   * Builds a list tree with all the elements which are stated in the xmlObject. If one of those elements already exist and the path is correct, then this element will not be added.
   * Adds the attribute "path" (KWS_CODES.PATH_ATTRIBUTE) to every "li" element, which contains its path along the list (and therefore the path of the folder in the explorer).
   * @param {XML} xmlObject the XML object with the new informations about elements in the root tree
   * @param {NODE} parentNode the node where the root tree is located
   */
  function buildTree(xmlObject, parentNode) {

    //create a "li" list object for every folder in the current folder (xmlObject)
    for (let o of xmlObject.childNodes) {
      if (o.hasChildNodes() == true && o.firstChild != null) {//first child is the folders (nodes) name
        var isParent = o.childNodes.length > 1; //first child node is its name, if it has more, it means there are sub folders

        //if the current element has the KWS_CODES.PATH_ATTRIBUTE with its current parents path in it, use that + its own name, otherwise if it does not have that attribute, it means it is the root folder and we will set it to empty ("")
        var cPath = parentNode.getAttribute(KWS_CODES.PATH) === null ? "" : parentNode.getAttribute(KWS_CODES.PATH) + o.firstChild.nodeValue;
        // var cPath = htmlNode.getAttribute(KWS_CODES.PATH_ATTRIBUTE)+o.firstChild.nodeValue;

        //find the element in the html document, which associates with the current object if existent
        var lin = getExplorerRootElementByPath(cPath);
        var found = (lin === null) ? false : true;

        // only have to add all the child elements if the corresponding li element was not found 
        if (!found) {
          // if not existent create the element and add the KWS_CODES.PATH_ATTRIBUTE
          lin = document.createElement("li");
          lin.setAttribute(KWS_CODES.PATH, cPath + "/");
          lin.classList.add(EXPLORER_ROOT_LI_ELEMENT);

          //create a wrapper for the icon and text to align both of them veritcally
          let lind = document.createElement("div");
          lind.classList.add(ROOT_ELEMENT_WRAPPER);

          //add the folder Icon as an image inside a div (to be able to control it from the css style sheet)
          var iconDiv = document.createElement("div");
          iconDiv.classList.add(ROOT_EXPLORER_ICON);

          //add the mouse listener, so that the folder will collapse or retract if clicked
          iconDiv.addEventListener("click", function () {

            //get the corresponding uln element of the icon (parent element is div containing icon and text --> one more parentElement is the li element and from here get the ul element)
            this.parentElement.parentElement.querySelector("." + NESTED).classList.toggle(ACTIVE);
            this.classList.toggle(ROOT_EXPLORER_ICON_OPEN);

            var temp = RootXML.getXMLelementByPath(RootXML.getRootXML(), pathToXPath(this.parentElement.parentElement.getAttribute(KWS_CODES.PATH)));

            //ajax the folder, if only one child node exist (which is the text node containg the folders name and therefore always present, hence ajax if child nodes are below 1)
            if (temp !== null && temp.childNodes.length <= 1) {
              console.log("no information on this folder, nneeeed AJAX!");

              // have to use this.parentElement.parentElement instead of the lin element, as the lin element will be rewritten and is always present as the last lin node created, hence all clicks on the folder will result in selecting the last lin node created)
              expandFolder(this.parentElement.parentElement.getAttribute(KWS_CODES.PATH));
            }
            else {
              console.log("Infos here????")
              console.log(temp === null)
              console.log(temp !== null)
              console.log(temp)
              console.log(this.parentElement.parentElement.getAttribute(KWS_CODES.PATH))
              console.log(temp.childNodes.length)
              console.log(temp.childNodes[0])
              console.log(temp.childNodes[1])
            }

          });

          //create span element with the folders name
          var sn = document.createElement("span");
          sn.classList.add(EXPANDABLE);
          sn.innerHTML += o.firstChild.nodeValue; //The folders name

          sn.addEventListener("click", function () {
            // have to use this.parentElement.parentElement instead of the lin element, as the lin element will be rewritten and is always present as the last lin node created, hence all clicks on the folder will result in selecting the last lin node created)
            expandFolder(this.parentElement.parentElement.getAttribute(KWS_CODES.PATH));
          });

          //create the uln element for further folder expansion
          var uln = document.createElement("ul");

          uln.classList.add(NESTED); //if element is created set its class as NESTED, which makes it a collapsed element
          uln.classList.add(EXPLORER_ROOT_UL_ELEMENT);
          uln.innerHTML = o.nodeValue;
          uln.setAttribute(KWS_CODES.PATH, lin.getAttribute(KWS_CODES.PATH));

          //append the icon (div element "imgn") and the folders name (span element "sn") to the div container lind, which is used to align both vertically before adding it to the li element
          lind.appendChild(iconDiv);
          lind.appendChild(sn);

          lin.appendChild(lind);
          lin.appendChild(uln);

          //append the created li element to the parent node
          parentNode.appendChild(lin);

          // console.log("created lin object")
          // console.log(lin);
        }

        if (isParent) {
          // console.log("parent directory, starting a new sub tree build");

          //buildTree gets called anyway, even if all elements were found, as maybe there was an element added further down the tree
          buildTree(o, getChildElement(lin, "ul"));
        }
      }
    }
  }







  /**
   * Retrieves the corresponding uln element of the span or div element(icon), which was selected and expands this folder by setting its class as active
   * @param {String} path the path of the folder which to expand
   */
  function expandFolder(path) {
    console.log("expand folder for path: " + path);

    //get the corresponding lin element for the path
    var lin = getExplorerRootElementByPath(path);


    //remove the previous current Folder class tag and add it to the current one (the div wrapper contained in the lin element)
    let divn = getChildElement(lin, "div");
    setCurrentFolder(divn);


    var parentLin = lin;
    while (parentLin.getAttribute(KWS_CODES.PATH) !== null) //stop loop, when the root element is exited and the parent would be the root window
    {
      // console.log(parentLin)
      //get the corresponding uln element of the icon or span element which was selected (parent element is div containing icon and text --> one more parentElement is the li element and from here get the ul element)
      var uln = parentLin.querySelector("." + NESTED);
      uln.classList.add(ACTIVE);

      //get the parent li node (parent folder) of the current directory and also set it to active. This is neccessary, to make sure the previous uln set to active is shown and not hidden by a parent ul  node which is not marked as active 
      parentLin = parentLin.parentElement.parentElement; //every li element is embedded in a ul element, hence go two parents up
      // console.log("new lin:")
      // console.log(parentLin.getAttribute(KWS_CODES.PATH_ATTRIBUTE))
      // console.log(parentLin.getAttribute(KWS_CODES.PATH_ATTRIBUTE) !== "null")
      // console.log(parentLin.getAttribute(KWS_CODES.PATH_ATTRIBUTE) !== null)
    }
    //add the class name ROOT_EXPLORER_ICON_OPEN to the div containing the icon to change its icon to an open folder
    uln.parentElement.querySelector("." + ROOT_EXPLORER_ICON).classList.add(ROOT_EXPLORER_ICON_OPEN);

    AJAXLib.ajaxExplorerDir(lin.getAttribute(KWS_CODES.PATH));
  }

  /**
   * adds the CURRENT_FOLDER class tag to this element (used for the current directory in the root tree panel) and removes the tag from all other elements.
   * In the css sheet, it is described, how the element in the root panel then should look like 
   * @param {Element} element 
   */
  function setCurrentFolder(element) {
    var elements = explorerRootUl.getElementsByTagName("div");
    for (let x of elements) {
      x.classList.remove(CURRENT_FOLDER);
    }
    element.classList.add(CURRENT_FOLDER);

  }


  /**
 * collapses all folders for the explorerRootPAnel by removeing every ul elements "active" class
 */
  function collapseExplorerFolders() {
    var elements = document.getElementsByTagName("ul");
    for (let x of elements) {
      x.classList.remove(ACTIVE);
    }
  }

  /**
   * gets all "li" elements in the document and looks up their KWS_CODES.PATH_ATTRIBUTE. If one mathes the path stated
   * this element will be returned
   * @param {String} path the path of the element which you want to retrieve
   * @returns the element with the KWS_CODES.PATH_ATTRIBUTE of the stated parameter path
   */
  function getExplorerRootElementByPath(path) {
    var elements = document.getElementsByTagName("li");

    //error handling (as a "/" will be added during the search to the path, make sure there is only one of this character)
    if (path.endsWith("/")) { path = path.substring(0, path.length - 1) }
    // console.log("looking for Path Object: " + path);

    for (let x of elements) {
      // console.log((x.getAttribute(KWS_CODES.PATH_ATTRIBUTE) === (path+"/"))+" --- "+path+"/ - "+ x.getAttribute(KWS_CODES.PATH_ATTRIBUTE))
      if (x.getAttribute(KWS_CODES.PATH) === path + "/") {
        // console.log("FOUND OBJECT FOR CURRENT PATH")
        return x;
      }
    }
    return null;
  }



  /**
   * creates a div element with the icon element as a figure in it. 
   * The str parameter will become the figure caption elemnet
   * @param {String} name the file or folders name
   * @param {String} itemType the path to the image, which should be shown
   * @returns a figure element containing the image on top and the name at the bottom (including various event listener)
   */
  function createExplorerItem(name, itemType) {

    // create the figure element as a wrapper for the image and text for the item
    var fign = document.createElement("figure");
    fign.classList.add(EXPLORER_FIGURE);

    //adjust the Path, if it is a folder, let it end with a "/"
    // console.log("CREATE OBJ"+currentDir+" --"+name)
    if (itemType === FOLDER_NAME) {
      fign.setAttribute(KWS_CODES.PATH, currentDir + name + "/");
      ExplorerMethods.addDnDListener(fign, true, true);
    } else {
      fign.setAttribute(KWS_CODES.PATH, currentDir + name);
      ExplorerMethods.addDnDListener(fign, true, false);
    }


    function dblcklickFunc() {
      switch (itemType) {

        case FOLDER_NAME:
          expandFolder(fign.getAttribute(KWS_CODES.PATH));
          break;

        case FILE_NAME:
          AJAXLib.downloadFile(fign.getAttribute(KWS_CODES.PATH));
          break
      }
    }


    function clickFunc(e) {
      if (e.getModifierState("Control")) {
        //add the selected element to the current selected elements
        selectExplorerElement(fign, true);
      } else if (e.getModifierState("Shift")) {
        //add all the elements in between the last selected element and the current selected element
        let startElement = getCurrentSelectedItems()[getCurrentSelectedItems().length - 1];

        let startIndex = parseInt(startElement.getAttribute(INDEX));
        let endIndex = parseInt(fign.getAttribute(INDEX));
        let diff = Math.abs(startIndex - endIndex);

        for (let t = 0; t <= diff; t++) {
          if (startIndex > endIndex) {
            selectExplorerElement(getElementByIndex(explorerMainPanel, (startIndex - t)), true);
          } else {
            selectExplorerElement(getElementByIndex(explorerMainPanel, (startIndex + t)), true);
          }
        }
      } else {
        //only select the element which was clicked
        selectExplorerElement(fign);
      }

      //prevent the event to reach the listener of the main panel, which deselects the element (which was just selected, so we need him to ignore it this once ;) 
      e.preventDefault();
      // e.stopImmediatePropagation();
      // e.stopPropagation;

    }


    fign.addEventListener("mouseenter", function () {
      mouseEnterExplorerElement(fign);
    })
    fign.addEventListener("mouseleave", function () {
      mouseLeaveExplorerElement(fign);
    })

    if(isMobileDevice){
      //only on mobile site, a single click triggers double click
      fign.addEventListener("contextmenu", clickFunc)
      //and a right click (hold) triggers a single click (the contextpopup is surpressed in the context init method if its a mobile page)
      fign.addEventListener("click", dblcklickFunc)
    }else{
      //otherwise normally click is click ^^
      fign.addEventListener("click", clickFunc)
    }
    
    //wont work on mobile devices, but these functions are implemented in other ways
    fign.addEventListener("dblclick", dblcklickFunc);
    


    //create the image element for the file or folder
    var imgdivn = document.createElement("img");
    var iconPath = FILE_ICON_PATH;
    if (itemType == FOLDER_NAME) {
      iconPath = FOLDER_ICON_PATH;
    }

    imgdivn.classList.add(EXPLORER_ELEMENT);
    imgdivn.title = name;
    imgdivn.alt = name;
    imgdivn.src = iconPath;
    imgdivn.setAttribute(KWS_CODES.PATH, fign.getAttribute(KWS_CODES.PATH)); //needed for the drag n drop listener, as the source will be this image if dragged

    //adjust the name, so that it will cut short with "..." at the end if the name is in general too long, or if on word is way too long, that would create visual overflow problems in the figcaption.
    name = adjustStringLength(name, 40, 15);

    //create item name as text 
    var figcaptn = document.createElement("figcaption");
    figcaptn.innerHTML = name;

    fign.appendChild(imgdivn);
    fign.appendChild(figcaptn);

    return fign;
  }


  /**
   * converts a regular path expresion into a xPath expression, where the drive and folder elements are not representing elements, but text nodes.
   * @param {String} path the path a la C:/path1/anotherPath 
   * @returns a String containg the path converted to the corresponding xPath
   * @example "C:/path1/anotherPath" will return "/*[text()='C:'/*[text()='path1'/*[text()='anotherPath'"
   */
  function pathToXPath(path) {
    // adjust the prePath for xPath
    var xPath = "/*[text()='" + ROOT_PATH_NAME + "']"; //start at /root

    for (var e of path.split("/")) {
      // console.log("now at:" + e)

      //following the most important and diffucult part to get a node from the names of the elements in the xml
      if (e !== "") {//makes sure, that "/C:/aPath" , "C:/aPath", "/C:/aPath" and "C:/aPath/" for exaple are all valid paths, as with the first and last "/" an empty array object will be created, which obviously has to be ignored
        xPath += "/*[text()='" + e + "']"; // "/*" selects every element in the current path and [text()='theNameYouWant'] only selects the elements of all the elements of "/*" where the text is "theNameYouWant"
      }
    }
    console.log(path + " ==> xPath: " + xPath);

    return xPath;
  }

  /**
 * get the element with the corresponding tag name (like img, span, p etc.). The method will search for
 * all the child nodes of the element with the tag name stated and if found return that element. 
 * @param {Element} element the element from which children you want to retrieve
 * @param {String} tagName the tag name of the child element you want
 */
  function getChildElement(element, tagName) {
    for (let x of element.childNodes) {
      if (tagName.toLowerCase() === x.tagName.toLowerCase()) { return x; }
    }
    return null;
  }


})();


const RootXML = (function () {

  /**
* The internal XMLFile (XML Object) which saves the explorer root directory tree. It starts only with the root element and is gradually expanding with every ajax request from the server where a directory is called
*/

  var rootTableXML;

  /**************
  *
  *   Public functions:
  *
  ***************/
  return {


    /**
     * initiates the rootTableXML object by creating a <root> element in it
     */
    initRootXML: function () {
      let xmlString = "<root>" + ROOT_PATH_NAME + "</root>"; //default root xml. into the root element the drivers will be placed (C:, D: and so on) as well as the substructures of them

      rootTableXML = new DOMParser().parseFromString(xmlString, "text/xml"); //important to use "text/xml"
    },


    getRootXML: function () {
      return rootTableXML;
    },

    /**
     * Searches the XML Object by the given xPath Parameter and returns its node
     * @param {XML} xml the XML Object
     * @param {String} xPath the path in a manner, as used in XPATH functions
     * @returns the XML element node if found by the xPath or null if not
     */
    getXMLelementByPath: function (xml, xPath) {
      try {//use try, as if xPath fails to be evaluated, it gets stuck (because the exception is not handled)


        //get the element node by evaluating the xpath
        var nodes = rootTableXML.evaluate(xPath, xml, null, XPathResult.ANY_TYPE, null);

        console.log("evaluatign xpath \"" + xPath + "\" succesful.");
        let result = nodes.iterateNext();
        return result;
      } catch (e) {
        console.log("getXMLelementByPath method: xpath error, probably xpath not found..." + e);
      }
      return null;
    },


    /**
     * update the rootTableXML containing the current explorer root tree for the new elements stated in the xml argument.<br>
     * The xml object should look like the one as stated in the testTree.xml as an example with a <root> element and its attribute "path", which tells its location in the tree and insinde 
     * <folder> and <file> elements.
     * @param {XML} xml the XML file you get from the Kira Server (example how it should look like is in testTree.xml) 
     */
    updateRootTableXML: function (xml) {
      console.log("updating internal root xml");

      //extract the path attribute from the xml we got from the server
      var prePpath = xml.getElementsByTagName("root")[0].getAttribute("path");

      // adjust the prePath for xPath
      var xPath = "/*[text()='" + ROOT_PATH_NAME + "']"; //start at /root

      var xPathParents = [];//all folders starting from /root until the parent folder of the path stated in the xml
      xPathParents.push(xPath);//add the root elements xPath

      console.log("splitting path to xPath");
      for (var e of prePpath.split("/")) {

        //following the most important and diffucult part to get a node from the names of the elements in the xml
        if (e !== "") {//makes sure, that "/C:/aPath" , "C:/aPath", "/C:/aPath" and "C:/aPath/" for exaple are all valid paths, as with the first and last "/" an empty array object will be created, which obviously has to be ignored
          xPath += "/*[text()='" + e + "']"; // "/*" selects every element in the current path and [text()='theNameYouWant'] only selects the elements of all the elements of "/*" where the text is "theNameYouWant"
          xPathParents.push(xPath);
        }
      }
      console.log("final xPath: " + xPath);

      //an array, which stores all the missing folders on the way from root to the path stated in the XML
      var notFound = new Array();

      var result;

      //create all the missing folders from root until the parent folder requested which are missing in our rootXML Object
      for (var i = xPathParents.length; i > 0; i--) {
        let cxPath = xPathParents[i - 1];
        try {//use try, as if xPath fails to be evaluated, it gets stuck (because the exception is not handled)

          //get the element node by evaluating the xpath
          var nodes = rootTableXML.evaluate(cxPath, rootTableXML, null, XPathResult.ANY_TYPE, null);
          // console.log("evaluatign xpath \""+cxPath+"\" succesful. Nodes found: " + nodes.length);
          result = nodes.iterateNext();

          //if there are results, continue
          if (result) {
            console.log("okokoko path exists. path has as many child nodes: " + result.childNodes.length);
            break;
          } else {
            console.log("no xpath results for " + cxPath + ", but function did not have an error, so... looking up a parent node");

            //check every time if we traced back the path all the way until the root element.
            //If yes, return the root node. 
            if (cxPath === "/" + ROOT_PATH_NAME) {
              var tesss = rootTableXML.evaluate("/*[text()='/root'", rootTableXML, null, XPathResult.ANY_TYPE, null);
              result = tesss.iterateNext();
              console.log("no parents found for the path and finally reached the root")
              // If not remember it in the notFound Array to create it later 
            } else {
              notFound.push(cxPath);
            }
          }
        } catch (e) {
          console.log("xpath error, prabably xpath not found..." + e);
          console.log(cxPath)
          console.log(rootTableXML)
        }
      }

      //should never happen (at least the root element should be found), but just in case
      if (result === null) {
        console.log("something went terribly wrong. Cant even find the root element in the rootXML Object. Was it even initialized???");
      } else {

        //create all missing intermediate folders for the tree
        console.log("creating missing folders in the tree for current path")
        try {
          //get the first parent node of the not found object (last object of the notFound Array, as the last added xPath has a parent which existed (and the second last xPath has the last xPath as its parent and so on))
          var cmd = notFound[notFound.length - 1];
          var parent = rootTableXML.evaluate(cmd.substring(0, cmd.lastIndexOf("/")), rootTableXML, null, XPathResult.ANY_TYPE, null);
          var pNode = parent.iterateNext();

          //insert all the other missing folders to the last created node
          for (var i = notFound.length - 1; i >= 0; i--) {
            cmd = notFound[i];

            //folder name is the String behind the last "/" (as stated in the path String e.g. "C:/path1/path2", "path2" is the last folder, so it comes after the last "/")
            var folderName = cmd.substring(cmd.lastIndexOf("/"));
            folderName = folderName.substring(folderName.indexOf("'") + 1, folderName.lastIndexOf("'"));
            console.log("creating folder: " + folderName)
            //insert the missing folder to the xml tree
            pNode = insertXMLelement(rootTableXML, pNode, FOLDER_NAME, folderName);
          }

          //last created element is node for all the folders in that folder
          result = pNode;

        } catch (e) {
          console.log("xpath error, could not create the missing folder in you tree.<br>" + e);
        }

        //get the folder names of the xml file to attach to the previously obtained rootTableXML node 
        var folders = RootXML.getXMLelements(xml, FOLDER_NAME);

        //create new folder node in explorerToorXML object for every folder stated in the retireved XML object
        for (var cf of folders) {
          insertXMLelement(rootTableXML, result, FOLDER_NAME, cf.childNodes[0].nodeValue);
        }
        console.log("appended all new folders to root XML");
      }
    },



    /**
     * Returns an array with all the elements, which correspondent to the given Tag in the xml file
     * @param {String} elementTag The tag name of the eleements you want to retrieve (like "li", "img", "p", "th" etc.)
     * @param {xml} xml The xml already parsed as xml object
     * @returns the array of all elements with that given Tag (empty Array if nothing matches)
     */
    getXMLelements: function (xml, elementTag) {
      var rawData = xml.getElementsByTagName(elementTag);

      var ret = new Array();
      if (rawData.length != 0) {
        for (let x of rawData) {
          ret.push(x);
        }
      }
      return ret;
    }



  }


  /**************
*
*   private functions:
*
***************/


  /**
   * Creates an element in the xml document stated and appends it at the node stated in the parentNode.
   * <br>
   * The element insert will be the style of <elementName>elementText</elementName>
   * @param {XML} xml the xml object where to insert the new element
   * @param {Node} parentNode the node where exactly to append the new element
   * @param {String} elementName the elements name (<elementName>)
   * @param {String} elementText the Text contained in the element
   * @returns the node of the just created element
   */
  function insertXMLelement(xml, parentNode, elementName, elementText) {
    var t = xml.createElement(elementName);
    var newText = xml.createTextNode(elementText);
    t.appendChild(newText);

    //append it to the correct place in the xml file
    parentNode.appendChild(t);

    return t;
  }


  /**
   * shows an alert window which contains the current RootXML text, so you can view the current explorer tree information as an xml file.
   * Has no real meaning, mostly just for debugging. 
   */
  function showXMLasAlert(xml) {
    // view new xml as an alert (just for debugging and checking, so usually commented ;)
    var xmlText = new XMLSerializer().serializeToString(xml);
    alert(xmlText);
    console.log(xmlText);
  }



})();








// right click menu -------------------------------------
// adjusted copy from https://www.sitepoint.com/building-custom-right-click-context-menu-javascript/
// tutorial for local libraries:
// https://stackoverflow.com/questions/2421911/what-is-the-purpose-of-wrapping-whole-javascript-files-in-anonymous-functions-li
const ContextMenuLib = (function () {



  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //
  // H E L P E R    F U N C T I O N S
  //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Function to check if we clicked inside an element with a particular class
   * name.
   * 
   * @param {Object} e The event
   * @param {String} className The class name to check against
   * @return {Boolean}
   */
  function clickInsideElement(e, className) {
    var el = e.srcElement || e.target;
    if (el.classList.contains(className)) {
      return el;
    } else {
      while (el = el.parentNode) {
        if (el.classList && el.classList.contains(className)) {
          return el;
        }
      }
    }

    return false;
  }

  /**
   * Get's exact position of event.
   * 
   * @param {Object} e The event passed in
   * @return {Object} Returns the x and y position
   */
  function getPosition(e) {
    var posx = 0;
    var posy = 0;

    if (!e) var e = window.event;

    if (e.pageX || e.pageY) {
      posx = e.pageX;
      posy = e.pageY;
    } else if (e.clientX || e.clientY) {
      posx = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
      posy = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }

    return {
      x: posx,
      y: posy
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //
  // C O R E    F U N C T I O N S
  //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Variables.
   */
  // var contextMenuClassName = "context-menu";
  var contextMenuItemClassName = "context-menu__item";
  var contextMenuActive = "context-menu--active";

  const ALLOW_CONTEXT_MENU = "allowContextMenu";

  var taskItemInContext;

  var clickCoords;
  var clickCoordsX;
  var clickCoordsY;

  var menu = document.querySelector("#context-menu");
  // var menuItems = menu.querySelectorAll(".context-menu__item");
  var menuState = 0;
  var menuWidth;
  var menuHeight;
  // var menuPosition;
  // var menuPositionX;
  // var menuPositionY;

  var windowWidth;
  var windowHeight;



  /**
   * Listens for contextmenu events.
 */
  function contextListener() {
    document.addEventListener("contextmenu", function (e) {
      taskItemInContext = clickInsideElement(e, ALLOW_CONTEXT_MENU);

      if (taskItemInContext) {
        e.preventDefault();
        toggleMenuOn();
        positionMenu(e);
      } else {
        taskItemInContext = null;
        ContextMenuLib.toggleMenuOff();
      }
    });
  }

  /**
   * Listens for click events. If an item was selected, lets the menuItemListener handle that event, otherwise when it is outside the menu, hides it
   */
  function contextMenuClickListener() {
    document.addEventListener("click", function (e) {
      var clickeElIsLink = clickInsideElement(e, contextMenuItemClassName);

      if (clickeElIsLink) {
        e.preventDefault();
        menuItemListener(clickeElIsLink);
      } else {
        // var button = e.which || e.button;
        // if (button === 1) {
        ContextMenuLib.toggleMenuOff();
        // }
      }
    });
  }

  /**
   * Listens for keyup events.
   */
  function keyupListener() {
    window.onkeyup = function (e) {
      if (e.keyCode === 27) {
        ContextMenuLib.toggleMenuOff();
      }
    }
  }

  /**
   * Window resize event listener
   */
  function resizeListener() {
    window.onresize = function (e) {
      ContextMenuLib.toggleMenuOff();
    };
  }

  /**
   * Turns the custom context menu on.
   */
  function toggleMenuOn() {
    if (menuState !== 1) {
      menuState = 1;
      menu.classList.add(contextMenuActive);
    }
  }

  /**
   * Positions the menu properly.
   * 
   * @param {Object} e The event
   */
  function positionMenu(e) {
    clickCoords = getPosition(e);
    clickCoordsX = clickCoords.x;
    clickCoordsY = clickCoords.y;

    menuWidth = menu.offsetWidth + 4;
    menuHeight = menu.offsetHeight + 4;

    windowWidth = window.innerWidth;
    windowHeight = window.innerHeight;

    if ((windowWidth - clickCoordsX) < menuWidth) {
      menu.style.left = windowWidth - menuWidth + "px";
    } else {
      menu.style.left = clickCoordsX + "px";
    }

    if ((windowHeight - clickCoordsY) < menuHeight) {
      menu.style.top = windowHeight - menuHeight + "px";
    } else {
      menu.style.top = clickCoordsY + "px";
    }
  }

  /**
   * Handles a click on the context menu and, depending on the item clicked, executes appropriate action
   * @param {HTMLElement} link The link that was clicked
   */
  function menuItemListener(link) {
    console.log("Context Menu action called")

    //If the item is not selectable, return without doing anything (e.g. download button, when no file is selected)
    if (!link.classList.contains(SELECTABLE)) { return; }

    //check the html for the correct command String
    switch (link.getAttribute("command")) {
      case "upload":
        ExplorerActions.uploadAction();
        break;

      case "download":
        ExplorerActions.downloadAction();
        break;

      case "copy":
        ExplorerActions.copyAction();
        break;

      case "cut":
        ExplorerActions.cutAction();
        break;

      case "paste":
        ExplorerActions.pasteAction();
        break;

    }

    //after item is selected, hide the menu
    ContextMenuLib.toggleMenuOff();
  }


  //define public methods (in this case only the initializing method to make the Context Menu appearable)
  /****************
   * 
   *  PUBLIC METHODS 
   * 
  *******************/

  let fileOperationItems = [document.getElementById("cm-download"),
  document.getElementById("cm-copy"), document.getElementById("cm-cut")];
  let pasteItem = document.getElementById("cm-cut");

  return {

    getContextMenuPasteItem() { return pasteItem; },


    /**
     * Initialise our application's code.
     */
    initContextMenu: function () {
      //only invoke of not mobile device
      if (!isMobileDevice) {
        contextListener();
        contextMenuClickListener();
        keyupListener();
        resizeListener();
      }
    },

    /**
      * Turns the custom context menu off.
      */
    toggleMenuOff: function () {
      if (menuState !== 0) {
        menuState = 0;
        menu.classList.remove(contextMenuActive);
      }
    },

    /**
     *enables operation commands which are file or folder specific, meaning which can only be executed if a item is selcted.
    * e.g. copy, paste, download etc.
    * also colors the items in the context menu and explorer menu bar to their original color to indicate, that they are available, by adding the "selectable" class to these items
    */
    enableFileOperations: function () {
      for (let ci of fileOperationItems) {
        ci.classList.add(SELECTABLE);
      }
      for (let ci of ExplorerActions.getMenuBarItems()) {
        ci.classList.add(SELECTABLE);
      }
    },

    /**
     *disables operation commands which are file or folder specific, meaning which can only be executed if a item is selcted.
    * e.g. copy, paste, download etc.
    * also greys out the items in the context menu and explorer menu bar to their original color to indicate, that they are unavailable, by removing the "selectable" class to these items
    */
    disableFileOperations: function () {
      for (let ci of fileOperationItems) {
        ci.classList.remove(SELECTABLE);
      }
      for (let ci of ExplorerActions.getMenuBarItems()) {
        ci.classList.remove(SELECTABLE);
      }
    }


  }
})();
// -------------------------------------------------------












//---------------------------------------------------------------------
//-----------------------mouse Events and Stuff-----------------------------------------------
//make the DIV element resizeable:-----------------------

/**
 * DEPRECATED
 * monitors the two bars left and bottom of the explorer called "resizer-bottom" and "resizer-bottom" and the point "resizer-bottomRight".
 * If the mouse is clicked there, the resizer will start and adjust the window size in regards, to where the mouse is
 * dragged while holding the mouse down.
 * 
 * This is deprecated, as the css function "resize: both;" is being used now
 */
const ResizerLib = (function () {

  let m_pos_x;
  let m_pos_y;

  //min window height and widths. Below these values, a window will not shrink
  const minWindowWdh = 100;
  const minWindowHgh = 100;

  /**
   * Resize a window depending on you mouse movement
   * @param {MouseEvent} e the mouse event
   * @param {boolean} resizeW true enables resizing horizontally
   * @param {boolean} resizeH true enables resizing vertically
   */
  function resizeWH(e, resizeW, resizeH) {
    e.preventDefault();
    // console.log("resizeing");

    const dx = m_pos_x - e.x;
    const dy = m_pos_y - e.y;

    m_pos_x = e.x;
    m_pos_y = e.y;

    // console.log(parseInt(getComputedStyle(explorer, '').width));
    var newWidth = parseInt(explorer.style.width.replace("px", "")) - dx;
    var newheight = parseInt(explorer.style.height.replace("px", "")) - dy;

    if (resizeW && newWidth > minWindowWdh) {
      explorer.style.width = newWidth + "px";
    }
    if (resizeH && newheight > minWindowHgh) {
      explorer.style.height = newheight + "px";
    }
  }

  const hListener = function (e) { resizeWH(e, false, true); };
  const wListener = function (e) { resizeWH(e, true, false); };
  const whListener = function (e) { resizeWH(e, true, true); };

  let enterResizing = false;


  const resizeStopListener = function () {
    document.removeEventListener("mousemove", hListener, false);
    document.removeEventListener("mousemove", wListener, false);
    document.removeEventListener("mousemove", whListener, false);
    enterResizing = false;
  }
  const resizeStartListener = function (e) {
    if (document.elementFromPoint(e.x, e.y).id == "resizer-bottom") {
      document.addEventListener("mousemove", hListener, false);
      enterResizing = true;
    } else if (document.elementFromPoint(e.x, e.y).id == "resizer-right") {
      document.addEventListener("mousemove", wListener, false);
      enterResizing = true;
    } else if (document.elementFromPoint(e.x, e.y).id == "resizer-bottomRight") {
      document.addEventListener("mousemove", whListener, false);
      enterResizing = true;
    }

    if (enterResizing) {
      m_pos_x = e.x;
      m_pos_y = e.y;
    }
  }


  return {

    startResizeListener: function () {
      // explorer.addEventListener("mousedown", resizeStartListener, false);
      document.addEventListener("mousedown", resizeStartListener, false);
      document.addEventListener("mouseup", resizeStopListener, false);
    }

  }

})();




/**
 * 
 * @param {String} str the string which should be adjusted
 * @param {int} maxLength the max length in chars (space included) which the string should be at the end (if larger, "..." will be added at the end). To ignore this value set it to 0 or a negative number
 * @param {int} maxWordLength if on word exceeds this size in chars, the string until that point will be returned with the last word until the maxWordLength's place and "..." added. To ignore this value set it to 0 or a negative number
 * @returns the adjusted string
 */
function adjustStringLength(str, maxLength, maxWordLength) {

  if (maxLength > 0 && str.length > maxLength) { str = str.substring(0, maxLength) + "..."; }
  var ret = "";

  //only treat positive values, otherwise it means, every word length should be accepted
  if (maxWordLength > 0) {
    for (let cw of str.split(" ")) {
      if (cw.length > maxWordLength) {
        ret += cw.substring(0, maxWordLength) + "...";
        return ret;
      }
      ret += cw + " ";
    }
  }

  return ret;
}



/**
 * The listener for the keys and their actions like delete, copy, paste, cut etc.
 * @param {Event} e event
 */
function keyListener(e) {

  //dont listen to the keys, if the explorer is hidden (closed)
  if (explorer.style.visibility === "hidden") {
    return;
  }


  //get modifiers as boolean if pressed
  let ctrl = e.getModifierState("Control");
  let alt = e.getModifierState("Alt");

  switch (e.keyCode) {

    //removed  due to same shortcut with browser
    // //arrow left
    // case 37:
    // if(alt){
    //   lastDirList.getPreviousDir();
    // }
    // break;

    // //arrow right
    // case 39:
    //   if(alt){
    //     lastDirList.getNextDir();
    //   }
    // break;
    //       //F5 --> refresh
    //       case 116:
    // AJAXLib.ajaxExplorerDir();
    //       break;


    //enter
    case 13:
      if (ExplorerActions.itemIsSelected() && getCurrentSelectedItems.size === 1) {

        //simulate double click on the selected item
        var event = new MouseEvent('dblclick', {
          'view': window,
          'bubbles': true,
          'cancelable': true
        });
        getCurrentSelectedItems()[0].dispatchEvent(event);
      }
      break;

    //left Arrow
    case 37:
      if (ExplorerActions.itemIsSelected()) {
        let ne = getElementByIndex(explorerMainPanel, parseInt(getCurrentSelectedItems()[0].getAttribute(INDEX)) - 1);
        if (ne !== null) {
          selectExplorerElement(ne);
        }
      }
      break;

    //up Arrow
    case 38:
      if (ExplorerActions.itemIsSelected()) {
        let columns = getColumnNumbers(explorerMainPanel);
        let newIndex = parseInt(getCurrentSelectedItems()[0].getAttribute(INDEX)) - columns;
        let ne = getElementByIndex(explorerMainPanel, newIndex);
        if (ne !== null) {
          selectExplorerElement(ne);
        }
      }
      break;

    //rigth Arrow
    case 39:
      if (ExplorerActions.itemIsSelected()) {
        let ne = getElementByIndex(explorerMainPanel, parseInt(getCurrentSelectedItems()[0].getAttribute(INDEX)) + 1);
        if (ne !== null) {
          selectExplorerElement(ne);
        }
      }
      break;

    //down Arrow
    case 40:
      if (ExplorerActions.itemIsSelected()) {
        let columns = getColumnNumbers(explorerMainPanel);
        let newIndex = parseInt(getCurrentSelectedItems()[0].getAttribute(INDEX)) + columns;
        let ne = getElementByIndex(explorerMainPanel, newIndex);
        if (ne !== null) {
          selectExplorerElement(ne);
        }
      }
      break;


    //delete
    case 46:
      ExplorerActions.deleteAction();
      break;


    //copy
    case 67:
      if (ctrl) {
        ExplorerActions.copyAction();
      }
      break;

    //paste
    case 86:
      if (ctrl) {
        ExplorerActions.pasteAction();
      }
      break;


    //cut
    case 88:
      if (ctrl) {
        ExplorerActions.cutAction();
      }
      break;

    //F2 --> Rename
    case 113:
      ExplorerActions.renameAction();
      break;


  }
}


function getColumnNumbers(element) {
  let columns = getComputedStyle(element).getPropertyValue('grid-template-columns');
  return columns.split(' ').length;
}

function getElementByIndex(panel, index) {
  let eles = panel.getElementsByTagName("figure");

  for (let ce of eles) {
    // console.log(parseInt(ce.getAttribute(INDEX), 10) + " : "+index)
    if (parseInt(ce.getAttribute(INDEX), 10) === index) {
      return ce;
    }
  }

  return null;

}

function getGridElementsPosition(element) {
  console.log("start")

  //Get the css attribute grid-template-columns from the css of class grid
  //split on whitespace and get the length, this will give you how many columns
  let eles = explorerMainPanel.getElementsByTagName("figure");
  console.log(eles.length)
  for (let ce of eles) {
    console.log(ce);

    if (ce === element) {
      console.log("found my element")
      let colCount = getColumnNumbers(explorerMainPanel);
      console.log(colCount);
      let index = parseInt(ce.getAttribute(INDEX), 10);
      console.log(index);

      let colPosition = index % colCount;
      let rowPosition = Math.floor(index / colCount);

      /* determine if it is a last column */
      if (colPosition == (colCount - 1)) {
        console.log('index:' + index + ' row:' + rowPosition + '. col:' + colPosition + '. Last column');
      } else {
        console.log('index:' + index + ' row:' + rowPosition + '. col:' + colPosition + '. Not last column');
      }
    }
  }

}


/**
 * Sets the background color of an figure (image and the figcaption) to another color (depending what kind of item) when the mouse hovers over it
 * @param {element} element 
 */
function mouseEnterExplorerElement(element) {
  var bg = null;
  var bd = null;

  if (!ExplorerActions.itemIsSelected() || !getCurrentSelectedItems().includes(element)) {

    switch (element.className) {
      case EXPLORER_FIGURE:
        bd = "1px solid rgb(23, 129, 216)";
        bg = "rgb(87, 162, 224)";
        break;

      case DESKTOP_FIGURE:
        bd = "1px solid rgb(23, 129, 216)";
        bg = "rgb(107, 228, 59)";
        break;
    }

    colorFigure(element, bd, bg);
  }

}

function mouseLeaveExplorerElement(element) {
  if (!ExplorerActions.itemIsSelected() || !getCurrentSelectedItems().includes(element)) {
    colorFigure(element, null, null);
  }
}

/**
 * Highlights an element for operations by storing it in a list ({@link getCurrentSelectedItems}) which will contain only this element if the parameter addElement=false (default).
 * You can store more elements by calling this method for each element you want to select and setting the addElement parameter to TRUE.
 * @param {DOMElement} element the element which should be highlighted and be used for operation
 * @param {Boolean} addElement default=FALSE; will add this element to the previous ones if set to TRUE. This way more than one element can be selected
 */
function selectExplorerElement(element, addElement = false) {

  if (ExplorerActions.itemIsSelected()) {
    for (let ce of getCurrentSelectedItems()) {
      colorFigure(ce, null, null);
    }
  }

  if (addElement && ExplorerActions.itemIsSelected()) {
    getCurrentSelectedItems().push(element);
    element = getCurrentSelectedItems();
  }

  setCurrentSelectedItem(element);

  for (let ce of getCurrentSelectedItems()) {
    colorFigure(ce, "1px solid rgb(23, 129, 216)", "rgb(60, 100, 59)");
  }
  //but at the same time hide the right click menu if it was opened (would be automatic, but as we stopped the propagation, we have to call it manually)
  ContextMenuLib.toggleMenuOff();
}


function removeAllCurrentSelectedItems() {
  currentSelectedItem = null;
  ContextMenuLib.disableFileOperations();
}

function setCurrentSelectedItem(element) {
  //insert the element into an array, if the element does not exist of one
  if (!Array.isArray(element)) {
    element = [element];
  }

  currentSelectedItem = element;
  ContextMenuLib.enableFileOperations();
}

/**
 * 
 * @returns the elements which are currently selected and on which an operation will be performed if called
 */
function getCurrentSelectedItems() { return currentSelectedItem; }


function colorFigure(figureElement, borderColor = "none", backgroundColor = "") {
  for (let x of figureElement.children) {

    if (borderColor === null) { borderColor = "none"; }
    if (backgroundColor === null) { backgroundColor = ""; }

    x.style.border = borderColor;
    x.style.backgroundColor = backgroundColor;
  }
}


// Make the DIV element draggable:--------------------------------------

class WindowDragger {

  #pos1 = 0; #pos2 = 0; #pos3 = 0; #pos4 = 0;
  #elmnt;
  #dragEnabled = false;

  /**
 * Makes the given element dragable by clicking on the and dragging on the given element. If it does not matter where you click inside the element, both values will be the same.
 * enable dragging by calling enableDrag() and likewise disableDrag() to disable it
 * @param {Element} dragElement the element which will get dragged as a whole when dragging is in progress
 * @param {Element} dragger the element from where you can drag (e.g. only from a special part inside the whole dragable element like a header in my example)
 */
  constructor(dragElement, dragger) {
    this.#dragFrame(dragElement, dragger);
  }

  /**
   * enables dragging for this Drag Element
   */
  enableDrag() { this.#dragEnabled = true; }


  /**
   * Disables dragging for this drag element
   */
  disableDrag() { this.#dragEnabled = false; }


  #dragMouseDown(e) {
    if (!this.#dragEnabled) { return; }

    e = e || window.event;
    e.preventDefault();

    // get the mouse cursor position at startup:
    this.#pos3 = e.clientX;
    this.#pos4 = e.clientY;
    document.onmouseup = this.#closeDragElement.bind(this);

    // call a function whenever the cursor moves:
    document.onmousemove = this.#elementDrag.bind(this);
  }

  #elementDrag(e) {
    e = e || window.event;
    e.preventDefault();

    // calculate the new cursor position:
    this.#pos1 = this.#pos3 - e.clientX;
    this.#pos2 = this.#pos4 - e.clientY;
    this.#pos3 = e.clientX;
    this.#pos4 = e.clientY;

    // set the element's new position:
    this.#elmnt.style.top = (this.#elmnt.offsetTop - this.#pos2) + "px";
    this.#elmnt.style.left = (this.#elmnt.offsetLeft - this.#pos1) + "px";
  }

  #closeDragElement() {

    // stop moving when mouse button is released:
    document.onmouseup = null;
    document.onmousemove = null;
  }

  /**
   * Makes the given element dragable by clicking on the and dragging on the given element. If it does not matter where you click inside the element, both values will be the same.
   * @param {Element} dragElement the element which will get dragged as a whole when dragging is in progress
   * @param {Element} dragger the element from where you can drag (e.g. only from a special part inside the whole dragable element like a header in my example)
   */
  #dragFrame(dragElement, dragger) {
    this.#elmnt = dragElement;
    if (dragger) {
      // if present, the dragger element is where you move the DIV from:
      dragger.onmousedown = this.#dragMouseDown.bind(this);

    } else {
      // otherwise, move the DIV from anywhere inside the DIV:
      this.#elmnt.onmousedown = this.#dragMouseDown.bind(this);
    }

  }
}


//-----------------------------

//initiate the page with all necessary event listener, setting optimal sizes, etc.
init();






















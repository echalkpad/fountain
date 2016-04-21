
//-----------------

let a = new Buffer([3,2,1]);
console.log("(a) Original buffer: ",a);

let s = a.toString("base64",0,a.length);
console.log("(s) Buffer a to new string s (base64 encoding specified):",s);

console.log("---");

let b2 = new Buffer(s);
console.log("(b2) Buffer b2 from string (no encoding specified): ",b2);  // go from here to [3,2.1]

let s2 = b2.toString();
console.log("(s2) Buffer b2 to string s2 (no encoding specified):",s);

let b3 = new Buffer(s2,'base64');
console.log("(b3) Buffer b3 from string s2 (base64 specified): ",b3[0],b3[1],b3[2]);

let b4 = new Buffer(b2.toString(),'base64');
console.log("(b4) Buffer b4 from buffer b2 (with intermediate string): ",b4);


// //-----------------

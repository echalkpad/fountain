  console.log("type of null:",typeof null);

  var obj1 = {"a":1,"b":2};
  console.log(obj1);
  t2(obj1);
  console.log(obj1);

  t3(1);
  t3(1,2);
  t3(1,2,3);
  t3(1,null,3);

  var bb = 0;
  var cc = !bb;

  var b2 = false;
  var c2 = !b2;

  var b3 = 'false';
  var c3 = !b3;

  console.log("bb,cc: ",bb,cc);
  console.log("b2,c2: ",b2,c2);
  console.log("b3,c3: ",b3,c3);

  t4(bb);
  t4(b2);
  t4(false);
  t4(true);
  t4("false");
  t4("true");

  t5();
  t5(null);
  t5(false);
  t5(true);
  t5(0);
  t5(1);

  for (var value in [true, false]) {
    console.log('value: ',value);
    t5(value);
  }



function t2(anObject) {
  anObject.c = 3;
}

function t3(a0, a1, a2) {
  console.log(arguments.length);
  console.log("a0: ",typeof a0,a0);
  console.log("a1: ",typeof a1,a1);
  console.log("a2: ",typeof a2,a2);
}

function t4(arg) {
  console.log("t4 arg, !arg: ", typeof arg, arg, !arg);
}

function t5(active) {
    var useNullLogger =
      (typeof active === 'undefined') ||
      (active === null ) ||
      (active === 0) || (active === '0') ||
      (active === false) || (active === 'false');

      console.log('active===false: ',active===false);
      console.log(typeof active, active, "=>",typeof useNullLogger,useNullLogger);
      console.log('---');
}

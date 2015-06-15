  var obj1 = {"a":1,"b":2};
  console.log(obj1);
  t2(obj1);
  console.log(obj1);

  t3(1);
  t3(1,2);
  t3(1,2,3);
  t3(1,null,3);

function t2(anObject) {
  anObject.c = 3;
}

function t3(a0, a1, a2) {
  console.log(arguments.length);
  console.log("a0: ",typeof a0,a0);
  console.log("a1: ",typeof a1,a1);
  console.log("a2: ",typeof a2,a2);
}

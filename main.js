let a;
function  fact_rec ( n ){if ( n <= 0 ) { return 1 ;} else{ return n*fact_rec ( n-1 )   ;};};
a = fact_rec ( 5 ) ;
console.log("a: " + a)

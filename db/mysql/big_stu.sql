use big_stu;
drop table if exists student;
create table student 
  (  s_id int(11) not null auto_increment ,
     sno    int(11), 
     sname varchar(50), 
     sage  int(11), 
     ssex  varchar(8) ,
     father_id int(11),
      mather_id int(11),
      note varchar(500),
     primary key (s_id),
   unique key uk_sno (sno)
  ) engine=innodb default charset=utf8mb4;
truncate table student;
  delimiter $$
drop function if exists   insert_student_data $$
create function insert_student_data()
 returns  int deterministic
    begin
    declare  i int;
      set i=1;
      while  i<=50000000 do 
      insert into student  values(i ,i, concat('name',i),i,case when floor(rand()*10)%2=0 then 'f' else 'm' end,floor(rand()*100000),floor(rand()*1000000),concat('note',i) );
      set i=i+1;
      end while;
      return 1;
    end$$
delimiter ;    
select  insert_student_data();
select count(*) from student;

use big_stu;
drop table if exists course;
create table course 
  ( 
     c_id int(11) not null auto_increment ,
     cname varchar(50),
     note varchar(500), 
     primary key (c_id)
  )  engine=innodb default charset=utf8mb4;
truncate table course;
  delimiter $$
drop function if exists insert_course_data $$
create function insert_course_data()
 returns  int deterministic
    begin
    declare  i int;
      set i=1;
      while  i<=1000 do 
      insert into course values(i , concat('course',i),concat('note',i) );
      set i=i+1;
      end while;
      return 1;
    end $$
delimiter ;    
select  insert_course_data();
select count(*) from course;

use big_stu;
drop table if exists sc;
create table sc 
  ( 
     s_id    int(11), 
     c_id    int(11), 
     t_id    int(11),
     score int(11) 
  )  engine=innodb default charset=utf8mb4;
truncate table sc;
  delimiter $$
drop function if exists insert_sc_data $$
create function insert_sc_data()
 returns  int deterministic
    begin
    declare  i int;
      set i=1;
      while  i<=50000000 do 
      insert into sc  values( i,floor(rand()*1000),floor(rand()*10000000),floor(rand()*750)) ;
      set i=i+1;
      end while;
      return 1;
    end $$
delimiter ;    
select  insert_sc_data();
commit;
create index idx_s_id  on sc(s_id)   ; 
create index idx_t_id  on sc(t_id)   ; 
create index idx_c_id  on sc(c_id)   ; 
select count(*) from sc;

use big_stu;
drop table if exists teacher;
create table teacher 
  ( 
    t_id  int(11) not null auto_increment ,
     tname varchar(50) ,
     note varchar(500),primary key (t_id)
  )  engine=innodb default charset=utf8mb4;

  truncate table teacher;
  delimiter $$
drop function if exists   insert_teacher_data $$
create function insert_teacher_data()
 returns  int deterministic
    begin
    declare  i int;
      set i=1;
      while  i<=10000000 do 
      insert into teacher  values(i , concat('tname',i),concat('note',i) );
      set i=i+1;
      end while;
      return 1;
    end$$
delimiter ;    
select  insert_teacher_data();
commit;
select count(*) from teacher;
select * from student;

select * from course;

select count(*) from big_stu.sc;

select * from teacher;

select count(*) from big_stu.student

-- 分页查询，起始页不一样
select * from big_stu.student limit 100,100; -- 0.051s
select * from big_stu.student limit 10000,100; -- 0.065s
select * from big_stu.student limit 100000,100; -- 0.145s
select * from big_stu.student limit 1000000,100; -- 1.051s
select * from big_stu.student limit 10000000,100; -- 10.458s

-- 针对上述问题，进行优化
-- 子查询优化
select s_id from big_stu.student limit 10000000,1; -- 3.355s
select * from big_stu.student where s_id >= (select s_id from big_stu.student limit 10000000,1) limit 100; -- 3.392s

-- 使用id限定优化（限定条件是id是连续递增的，根据查询的页数和查询的记录数可以算出查询的id的范围）
select * from big_stu.student where s_id between 10000000 and 10000100 limit 100;-- 0.063s
select * from big_stu.student where s_id >= 10000000 limit 100;-- 0.053s
select * from big_stu.student where s_id in(select s_id from big_stu.student) limit 100; -- 0.058s

-- 使用临时表优化
-- 大概的思路就是使用临时表记录分页的id，使用分页的id进行in查询



nums = list(range(2, 50)) 
for i in range(2, 8): 
     nums = [x for x in nums if x == i or x % i]
 
print(nums)
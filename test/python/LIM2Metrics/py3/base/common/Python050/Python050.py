from array import array

a = array('H', [4000, 10, 700, 22222])
sum(a)

a[1:3]
array('H', [10, 700])

# Bit
x = 1        # 0001
x << 2       # shift left 2 bits: 0100
print(x)

#Complex
print(1j * 1J)
print(1j * complex(0,1))
print(3+1j*3)
print((3+1j)*3)
print((1+2j)/(1+1j))

#Float
print(3 * 3.75 / 1.5)
7.5
print(7.0 / 2)

print(oct(64), hex(64), hex(255))

def c( p ) :
       spy = 60 * 60 * 24 * 365.2422
       n = int ( spy ) * int ( p )
       return n

if __name__ == "__main__" :
        n = c ( 186000 )
        print(n)
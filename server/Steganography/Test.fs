module Test

open NUnit.Framework

[<TestFixture>]
type Test() =

    [<Test>]
    member this.``2 adds 2 should equals 4``() = 
        Assert.AreEqual(4, 2 + 2)


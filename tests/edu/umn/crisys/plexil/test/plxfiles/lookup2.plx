<?xml version="1.0" encoding="UTF-8"?><!-- Generated by PlexiLisp --><PlexilPlan xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tr="extended-plexil-translator"><Node NodeType="Assignment"><NodeId>lookup2</NodeId><VariableDeclarations>
      <DeclareVariable>
        <Name>x</Name>
        <Type>Integer</Type>
      </DeclareVariable>
      <DeclareVariable>
        <Name>state-name</Name>
        <Type>String</Type>
        <InitialValue>
<StringValue>foo</StringValue>
        </InitialValue>
      </DeclareVariable>
    </VariableDeclarations><NodeBody>
      <Assignment>
        <IntegerVariable>x</IntegerVariable>
        <NumericRHS>
          <LookupNow>
            <Name>
              <StringVariable>state-name</StringVariable>
            </Name>
          </LookupNow>
        </NumericRHS>
      </Assignment>
    </NodeBody></Node></PlexilPlan>
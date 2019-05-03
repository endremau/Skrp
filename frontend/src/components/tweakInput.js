import React, { Component } from 'react'
import Parameters from './parameter'
import styled from 'styled-components'

const Form = styled.form`
top -border: 50%;
display:flex;
flex-direction:row;
flex-wrap: wrap;
`

class TweakInput extends Component {
  constructor (props) {
    super(props)
    this.device_id = {}
    this.state = {
    }
    this.props = props
  }

  componentWillMount(){
    this.setInitalState()
  }

  async setInitalState (){
  const stateToBe = await fetch('http://localhost:8090/configure').then(data => { return data.json()}).catch(err => console.error(err))
  let parameters = stateToBe[0]["config"]
  let device_id = stateToBe[0]["device_id"]
  //this is not allowed, plz fix me.
  // This might trigger alot of state updates
  this.device_id = device_id
  Object.entries(parameters).map(p=>(
    this.setState({
        [p[0]]:p[1],
    })
  )
  )}
    
  sendToConfigure(){
    let state = this.state
    let statement = `?device_id=${this.device_id}&`
    Object.entries(state).map(x=>statement+=x[0]+"="+x[1]+"&")
    statement = statement.substring(0, statement.length-1)
    fetch("http://localhost:8090/configure"+statement)
  }


  sendToAdaptation(name,value){
    let id = 2
    let description = `parameter ${name} has been changed to ${value}`
    console.log(description)
    let statement = `http://localhost:8090/logadaption?adaption_type=${name}&device_id=${id}&description=${description}`
    fetch (statement,{method:"POST"})
  }
  valdiator (value) {
    return value.match(/^(0(\.\d+)?|[0-9]+)$/)
  }

    onChangeParameterValue(name, event){
      let value = event.target.value
      if (event.key === 'Enter' && this.valdiator(value) )  {
        this.setState({
          [name]: value
        })
        setTimeout(() => {
          this.sendToConfigure()
          this.sendToAdaptation(name,value)
        }, 1);
      }}

    render () {
      let state = Object.entries(this.state)
      let parameters = state.map(s => {
        return <Parameters data-cy="submit" key={s[0]} changeParameterValue={this.onChangeParameterValue.bind(this, s[0])} parameter={s[1]} name ={s[0]}/>
      })
      return <div>
      <Form className="flex-container">
      {parameters}
      </Form>
      </div>
    }
}

export default TweakInput
